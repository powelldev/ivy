package fireminder.podcastcatcher.services;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.PowerManager;
import android.util.Log;
import fireminder.podcastcatcher.PodcastCatcher;
import fireminder.podcastcatcher.db.EpisodeDao;
import fireminder.podcastcatcher.db.PodcastDao;
import fireminder.podcastcatcher.downloads.BackgroundThread;
import fireminder.podcastcatcher.utils.Helper;
import fireminder.podcastcatcher.utils.RssParser;
import fireminder.podcastcatcher.utils.Utils;
import fireminder.podcastcatcher.valueobjects.Episode;

public class ADownloadService extends IntentService {
    
    PodcastDao pdao = new PodcastDao();
    EpisodeDao edao = new EpisodeDao();

    public ADownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Utils.log("HAPT", "DownloadService started at " + System.currentTimeMillis() + "");
        PodcastCatcher.getInstance().setContext(getApplicationContext());
        BackgroundThread bt = new BackgroundThread(this);
        bt.getNewEpisodes();
//        
//        BroadcastReceiver receiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                Intent i = new Intent();
//                i.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
//                startActivity(i);
//            }
//        };
//        
//        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
//        Log.e("DOWNLOADSERVICE", "From dls");
//        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
//        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
//        wl.acquire();
//        List<Episode> result = doInBackground();
//        onPostExecute(result);
//        wl.release();

    }



    protected List<Episode> doInBackground() {

        Cursor cursor = null;
        cursor = pdao.getAllPodcastsAsCursor();

        cursor.moveToFirst();

        List<Episode> indivEpisodes = null;
        List<Episode> episodes = new ArrayList<Episode>();
        do {
            Episode e = null;
            e = edao.getLatestEpisode(cursor.getLong(cursor
                    .getColumnIndex(PodcastDao.COLUMN_ID)));
            Log.e("IntentService",
                    ""
                            + cursor.getString(cursor
                                    .getColumnIndex(PodcastDao.COLUMN_LINK)));

            try {
                URL url = new URL(cursor.getString(cursor
                        .getColumnIndex(PodcastDao.COLUMN_LINK)));
                HttpURLConnection urlConn = (HttpURLConnection) url
                        .openConnection();
                InputStream is = urlConn.getInputStream();

                indivEpisodes = RssParser.parseNewEpisodesFromXml(is, cursor
                        .getInt(cursor
                                .getColumnIndex(PodcastDao.COLUMN_ID)), e
                        .getPubDate());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (indivEpisodes.size() != 0) {
                for (Episode episode : indivEpisodes) {
                    episode = edao.get(edao.insert(e));
                    episodes.add(episode);
                    Log.d("IntentService", episode.getTitle() + " " + episode.getPubDate());
                    Utils.log("IntentService:" + episode.getTitle() + " " + episode.getPubDate());
                }
            }

        } while (cursor.moveToNext());
        return episodes;
    }

    protected void onPostExecute(List<Episode> result) {
        if (result == null) { 
            Log.e("DLs", "No New episodes");
            return; 
            }
        for (Episode e : result) {
            Helper.downloadEpisodeMp3(e);
        }
    }
    
    /*public void downloadEpisodeMp3(Episode e) {
        String fileName = e.getUrl();
        fileName = fileName.substring(fileName.lastIndexOf("/"));
        Log.d("Downloading...", fileName);
        // TODO Make download notification point back at app - or allow
        // cessation of download
        final DownloadManager dm = (DownloadManager) this
                .getSystemService(Context.DOWNLOAD_SERVICE);
        
        Request request = new Request(Uri.parse(e.getUrl()));
        request.setTitle(e.getTitle())
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDescription("Touch to cancel")
                .setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_PODCASTS, fileName);
        long enqueue = dm.enqueue(request);
        e.setMp3(Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_PODCASTS + fileName);
        edao.update(e);
    }
    */

}
