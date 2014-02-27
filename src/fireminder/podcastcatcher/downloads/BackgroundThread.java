package fireminder.podcastcatcher.downloads;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.util.ByteArrayBuffer;
import org.xmlpull.v1.XmlPullParserException;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import fireminder.podcastcatcher.OnTaskCompleted;
import fireminder.podcastcatcher.db.EpisodeDao;
import fireminder.podcastcatcher.db.PodcastDao;
import fireminder.podcastcatcher.utils.Helper;
import fireminder.podcastcatcher.utils.RssParser;
import fireminder.podcastcatcher.utils.Utils;
import fireminder.podcastcatcher.valueobjects.Episode;
import fireminder.podcastcatcher.valueobjects.Podcast;

/***
 * Encapsulation of the AsyncTasks this application utilizes.
 */
public class BackgroundThread {

    final static String TAG = BackgroundThread.class.getSimpleName();

    private Context context;
    private PodcastDao pdao;
    private EpisodeDao edao;
    public BackgroundThread(Context context) {
        this.context = context;
        pdao = new PodcastDao(context);
        edao = new EpisodeDao(context);
    }

    /***
     * launches AsyncTasks for adding episodes to episode database
     * 
     * @param url
     * @param id
     */
    public void getEpisodesFromBackgroundThread(String url, long id) {
        new ParseXmlForEpisodes().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR, new String[] { url, "" + id });
    }

    /***
     * launches AsyncTask for loading a podcast image
     * 
     * @param url
     * @param id
     */
    public void getPodcastImageFromBackgroundThread(String url, long id, OnTaskCompleted listener) {
        new ParseXmlForImage(listener).execute(new String[] { url, "" + id });
    }

    /***
     * AsyncTask responsible for downlaoding an MP3.
     */
    private class Mp3DownloadTask extends
            AsyncTask<Episode, Void, ByteArrayBuffer> {

        protected void onPreExecute() {
        }

        protected ByteArrayBuffer doInBackground(Episode... episodes) {

            Episode episode = episodes[0];
            File file = new File(Environment.DIRECTORY_PODCASTS
                    + episode.getTitle() + ".mp3");

            if (!file.exists()) {
                DownloadManager dm = (DownloadManager) context
                        .getSystemService(Context.DOWNLOAD_SERVICE);
                Request request = new Request(Uri.parse(episode.getUrl()));
                request.setTitle(episode.getTitle())
                        .setDescription("Touch to Cancel")
                        .setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_PODCASTS,
                                episode.getTitle() + ".mp3");
                long enqueue = dm.enqueue(request);
            } else {
                Log.d("Mp3 already exists", Environment.DIRECTORY_PODCASTS
                        + episode.getTitle() + ".mp3");
            }
            return null;
        }

        protected void onPostExecute(ByteArrayBuffer result) {

        }
    }

    /***
     * AsyncTask responsible for parsing xml page for image
     */
    private class ParseXmlForImage extends AsyncTask<String, Void, Void> {
        public ParseXmlForImage(OnTaskCompleted activity) {
            this.listener = activity;
        }
        OnTaskCompleted listener;
        String idForQuery;

        @Override
        protected Void doInBackground(String... urls) {
            URL url;
            idForQuery = urls[1];
            String imagelink = null;
            try {
                url = new URL(urls[0]);
                HttpURLConnection con = (HttpURLConnection) url
                        .openConnection();
                InputStream is = con.getInputStream();
                imagelink = RssParser.parsePodcastImageFromXml(is);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Podcast podcast = pdao.get(Long.parseLong(idForQuery));
            if (imagelink != null) {
                podcast.setImagePath(imagelink);
            } else {
                podcast.setImagePath("http://static.tvtropes.org/lampshade_logo_blue.png");
            }
            pdao.update(podcast);
            listener
                    .onTaskCompleted(null);
            return null;
        }

    }

    /***
     * AsyncTask responsible for grabbing individual episodes from a podcast's
     * RSS.
     */
    private class ParseXmlForEpisodes extends AsyncTask<String, Void, Void> {
        long idForQuery;

        @Override
        protected Void doInBackground(String... urls) {
            URL url;
            idForQuery = Long.parseLong(urls[1]);
            BufferedReader reader = null;
            List<Episode> episodes = null;
            try {
                url = new URL(urls[0]);
                HttpURLConnection con = (HttpURLConnection) url
                        .openConnection();
                InputStream is = con.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is));
                Log.d("EpisodeParsing", "here: " + urls[0] + " " + urls[1]);
                // episodes = RssParser.parseEpisodesFromXml(reader,
                // idForQuery);
                episodes = RssParser.parseEpisodesFromXml(is, idForQuery);
                for (Episode episode : episodes) {
                    Log.d("Inserting: ", episode.getTitle());
                    edao.insert(episode);
                    Helper.isDownloaded(episode, context);
                }

                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

    }

    /*** Wrapper for getting new episodes */
    public void getNewEpisodesForPodcast(int podcast_id) {
        new CheckXmlForNewEpisodesForPodcast().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR, new Integer[] { podcast_id });
    }

    /***
     * AsyncTask that checks for newest episodes by comparing the latest
     * episode's pubdate to those in the RSS stream. It ceases looking when it
     * finds a episode with a date older than that one.
     */
    private class CheckXmlForNewEpisodesForPodcast extends
            AsyncTask<Integer, Void, Episode[]> {

        @Override
        protected Episode[] doInBackground(Integer... params) {
            Integer podcast_id = params[0];

            Podcast podcast = pdao.get(podcast_id);
            Episode latest = edao.getLatestEpisode(podcast_id);

            // For each Podcast, get the latest episode's pubDate

            List<Episode> episodes = null;
            Log.d(TAG, "Latest Epi:" + latest.getTitle() + latest.getPubDate());
            try {

                URL url = new URL(podcast.getLink());
                HttpURLConnection urlConn = (HttpURLConnection) url
                        .openConnection();
                InputStream is = urlConn.getInputStream();

                episodes = RssParser.parseNewEpisodesFromXml(is,
                        podcast.getId(), latest.getPubDate());
                // TODO WORKING HERE
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (episodes != null) {
                for (Episode episode : episodes) {
                    episode = edao.get(edao.insert(episode));
                    Log.d(TAG, episode.getTitle() + " " + episode.getPubDate());
                }
            }
            // Open Podcast's URL
            // Parse Podcast's url for episodes until an episodes pubDate is <=
            // current latest pubDate
            return (Episode[]) episodes.toArray();
        }

    }

    public void getNewEpisodes() {
        new CheckXmlForNewEpisodes().execute();
    }

    private class CheckXmlForNewEpisodes extends
            AsyncTask<Void, Void, Episode[]> {

        @Override
        protected Episode[] doInBackground(Void... params) {

            Cursor cursor = null;
            // Get a List of Podcasts
            cursor = pdao.getAllPodcastsAsCursor();

            cursor.moveToFirst();

            List<Episode> episodes = new ArrayList<Episode>();
            List<Episode> indivEpisodes = null;
            try {
                do {
                    Episode e = null;
                    e = edao.getLatestEpisode(cursor.getLong(cursor
                            .getColumnIndex(PodcastDao.COLUMN_ID)));
                    Log.e(TAG,
                            ""
                                    + cursor.getString(cursor
                                            .getColumnIndex(PodcastDao.COLUMN_LINK)));

                    URL url = new URL(cursor.getString(cursor
                            .getColumnIndex(PodcastDao.COLUMN_LINK)));
                    HttpURLConnection urlConn = (HttpURLConnection) url
                            .openConnection();
                    InputStream is = urlConn.getInputStream();

                    indivEpisodes = RssParser.parseNewEpisodesFromXml(is,
                            cursor.getInt(cursor
                                    .getColumnIndex(PodcastDao.COLUMN_ID)), e
                                    .getPubDate());

                    if (indivEpisodes.size() != 0) {
                        for (Episode episode : indivEpisodes) {
                            episode = edao.get(edao.insert(episode));
                            // new BackgroundThread(context)
                            // .downloadEpisodeMp3(episode);
                            Helper.downloadEpisodeMp3(episode, context);
                            Log.d("HAPT",
                                    episode.getTitle() + " "
                                            + episode.getPubDate());
                        }
                    }

                } while (cursor.moveToNext());

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Episode[] array;
            if (episodes != null || episodes.size() > 0) {
                array = episodes.toArray(new Episode[episodes.size()]);
            } else {
                array = null;
            }
            return array;
        }

        @Override
        protected void onPostExecute(Episode[] result) {
            super.onPostExecute(result);
            for (Episode e : result) {
                Helper.downloadEpisodeMp3(e, context);
            }
            Log.e("Finished", "Getting new episodes");
        }
    }

    /***
     * AsyncTask responsible for searching iTunes podcast directory for a
     * podcast
     */
    public class Search extends AsyncTask<String, Void, List<String>> {
        OnTaskCompleted listener;

        public Search(OnTaskCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected List<String> doInBackground(String... params) {
            List<String> results = null;

            InputStream is = null;
            try {
                URL url = new URL(params[0]);
                Log.e(TAG, params[0]);
                HttpURLConnection urlConn = (HttpURLConnection) url
                        .openConnection();
                urlConn.setConnectTimeout(10000);
                is = urlConn.getInputStream();
            } catch (java.net.SocketTimeoutException se) {
                return null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(is));
            } catch (Exception e) {
                return null;
            }
            results = Helper.parseJSONforPodcasts(reader);
            return results;
        }

        @Override
        public void onPostExecute(List<String> result) {
            if (result == null) {
                Toast.makeText(context, "Not found", Toast.LENGTH_LONG).show();
            }
            listener.onTaskCompleted(result);

        }

    }

    /*** Wrapper for iTunes Search AsyncTask */
    public void searchItunesForPodcasts(String searchURL) {
        new Search((OnTaskCompleted) context).executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR, new String[] { searchURL });

    }

    public void downloadAll(long podcast_id) {
        List<Episode> episodes = edao.getAllEpisodes(podcast_id);
        for (Episode episode : episodes) {
            Helper.downloadEpisodeMp3(episode, context);
        }
    }

    public class SubscribeAsyncTask extends AsyncTask<String, Void, Podcast> {
        public SubscribeAsyncTask(OnTaskCompleted activity) {
            sListener = activity;
            
        }
        private OnTaskCompleted sListener;
        private long id;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            id = pdao.insert(new Podcast("Loading ..."));
            sListener.onTaskCompleted(null);
        }

        @Override
        protected Podcast doInBackground(String... urls) {

            BufferedReader reader = null;
            Podcast podcast = null;
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection con = (HttpURLConnection) url
                        .openConnection();
                InputStream is = con.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is));
                podcast = RssParser.parsePodcastFromXml(is);
                podcast.setLink(urls[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                return null;
            }
            return podcast;
        }

        @Override
        protected void onPostExecute(Podcast result) {
            pdao.delete(pdao.get(id));
            if (result != null) {
                Podcast podcast = pdao.get(pdao.insert(result));
                BackgroundThread bt = new BackgroundThread(context);
                bt.getEpisodesFromBackgroundThread(podcast.getLink(),
                        podcast.getId());
                bt.getPodcastImageFromBackgroundThread(podcast.getLink(),
                        podcast.getId(), sListener);
                sListener.onTaskCompleted(null);
                
            } else {
                Toast.makeText(context,
                        "Podcast subscription failed: Please check url",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public void subscribeToPodcast(String url, OnTaskCompleted listener) {
        new SubscribeAsyncTask(listener).execute(new String[] { url });
    }

    public void parseOpmlForPodcasts(File file, OnTaskCompleted listener) {
        new OpmlAsyncTask(listener).execute(new File[] { file });
    }

    private class OpmlAsyncTask extends AsyncTask<File, Void, String[]> {
        private OnTaskCompleted listener; 
        public OpmlAsyncTask (OnTaskCompleted activity) {
            listener = activity;
        }

        @Override
        protected String[] doInBackground(File... args) {
            List<String> podcasts = null;
            try {
                FileInputStream fis = new FileInputStream(args[0]);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(fis));
                podcasts = RssParser.parseOpmlForPodcasts(reader);
            } catch (Exception e) {
                Log.e(Utils.TAG, "Err opmlasync: " + e.getMessage());
            }
            String[] arr = new String[podcasts.size()];
            for (String p : podcasts) {
                arr[0] = p;
            }

            return arr;
        }
        
        @Override
        protected void onPostExecute(String[] result){
            for(String s : result) {
                subscribeToPodcast(s, listener);
            }
        }

    }

}
