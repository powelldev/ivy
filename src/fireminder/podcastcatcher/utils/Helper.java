package fireminder.podcastcatcher.utils;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import fireminder.podcastcatcher.activities.MainActivity;
import fireminder.podcastcatcher.db.EpisodeDao;
import fireminder.podcastcatcher.downloads.BackgroundThread;
import fireminder.podcastcatcher.valueobjects.Episode;

public class Helper {

    public static boolean updateIfDownloadedAlready(Episode e, Context context) {
        String fileName = e.getUrl();
        fileName = fileName.substring(fileName.lastIndexOf("/"));
        fileName = fileName.replaceAll("\\.", "_");
        fileName = fileName.replaceAll("%20", "_");
        fileName = fileName.replaceAll("_mp3", ".mp3");
        File file = new File(Environment.getExternalStorageDirectory()
                .getPath() + "/" + Environment.DIRECTORY_PODCASTS + fileName);
        if (file.exists()) {
            e.setMp3(file.getAbsolutePath());
            new EpisodeDao(context).update(e);
            return true;
        } else {
            return false;
        }
    }
    public static void downloadEpisodeMp3(Episode e, Context context) {
        String fileName = e.getUrl();
        fileName = fileName.substring(fileName.lastIndexOf("/"));
        fileName = fileName.replaceAll("\\.", "_");
        fileName = fileName.replaceAll("%20", "_");
        fileName = fileName.replaceAll("_mp3", ".mp3");
        File file = new File(Environment.getExternalStorageDirectory()
                .getPath() + "/" + Environment.DIRECTORY_PODCASTS + fileName);
        if (file.exists()) {
            e.setMp3(file.getAbsolutePath());
        } else {
            Log.e("Downloading...", fileName);
            // TODO Make download notification point back at app - or allow
            // cessation of download
            final DownloadManager dm = (DownloadManager) context
                    .getSystemService(Context.DOWNLOAD_SERVICE);

            Request request = new Request(Uri.parse(e.getUrl()));
            request.setTitle(e.getTitle())
                    .setNotificationVisibility(
                            DownloadManager.Request.VISIBILITY_VISIBLE)
                    .setDescription("Touch to cancel")
                    .setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_PODCASTS, fileName);
            long enqueue = dm.enqueue(request);
            Utils.log("Downloading id " + enqueue + " : " + e.getTitle());
            e.setMp3(Environment.getExternalStorageDirectory().getPath() + "/"
                    + Environment.DIRECTORY_PODCASTS + fileName);
        }
        new EpisodeDao(context).update(e);
    }

    public static void searchForPodcasts(Context context, String term) {
        String searchURL = String
                .format("https://itunes.apple.com/search?media=podcast&term=%s&attribute=titleTerm",
                        term);
        Log.d("SearchURL: ", searchURL);
        BackgroundThread bt = new BackgroundThread(context);
        bt.searchItunesForPodcasts(searchURL);
    }

    public static JSONObject getJsonObject(BufferedReader reader) {
        StringBuilder sb = new StringBuilder();
        String line = null;
        JSONObject jsonObj = null;
        String json = "";
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            json = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            jsonObj = new JSONObject(json);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObj;
    }

    private class PodcastResult {
        static final String NAME = "collectionName";
        static final String ARTIST = "artistName";
        static final String FEED_URL = "feedUrl";
        static final String IMAGE_URL_30 = "artworkUrl30";
        static final String IMAGE_URL_60 = "artworkUrl60";
        static final String IMAGE_URL_100 = "artworkUrl100";
    }

    public static List<String> parseJSONforPodcasts(BufferedReader reader) {

        List<String> messages = new ArrayList<String>();

        JSONObject jObj = getJsonObject(reader);
        // TODO: Encapsulate NAME, ARTIST and URL into an object

        final String NAME = "collectionName";
        final String ARTIST = "artistName";
        final String FEED_URL = "feedUrl";

        JSONArray podcast = null;

        try {
            podcast = jObj.getJSONArray("results");
            for (int i = 0; i < podcast.length(); i++) {
                JSONObject obj = podcast.getJSONObject(i);
                Log.d("JSON: ", obj.getString(PodcastResult.NAME));
                Log.d("JSON: ", obj.getString(PodcastResult.ARTIST));
                Log.d("JSON: ", obj.getString(PodcastResult.FEED_URL));
                messages.add(obj.getString(NAME));
                messages.add(obj.getString(ARTIST));
                messages.add(obj.getString(FEED_URL));
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        /*
         * JsonReader jsonReader = new JsonReader(reader); try {
         * jsonReader.beginObject(); Log.d("JSON: ", jsonReader.nextName());
         * Log.d("JSON: ", jsonReader.nextString()); Log.d("JSON: ",
         * jsonReader.nextName()); jsonReader.beginArray();
         * jsonReader.beginObject(); JsonToken jtok = null;
         * while(jsonReader.hasNext() || found == ITEMS_FOUND){ jtok =
         * jsonReader.peek(); Log.d("jtok: ", jtok.name());
         * if(jtok.equals(JsonToken.NAME)){ Log.d("jsonReader: ",
         * jsonReader.nextName()); } else if (jtok.equals(JsonToken.STRING)) {
         * Log.d("jsonReader: ", jsonReader.nextString()); } else if
         * (jtok.equals(JsonToken.NUMBER)) { Log.d("jsonReader: ", "" +
         * jsonReader.nextInt()); } else { Log.d("jsonReader: ",
         * jsonReader.nextName()); }
         * 
         * if(jsonReader.nextName().contains("trackName")){
         * 
         * messages.add(jsonReader.nextString());
         * 
         * found++; } else if(jsonReader.nextName().matches("feedUrl")){
         * messages.add(jsonReader.nextString()); found++; } else{
         * jsonReader.nextName();
         * 
         * }
         * 
         * }
         * 
         * jsonReader.close(); } catch (IOException e) { // TODO Auto-generated
         * catch block e.printStackTrace(); }
         */
        return messages;
    }

    public static void getNewEpisodesFromPodcast(Context context, long itemId) {

        BackgroundThread bt = new BackgroundThread(context);
        bt.getNewEpisodesForPodcast((int) itemId);
    }
    public static void parseOpmlForPodcasts(File file, MainActivity activity) {
        BackgroundThread bt = new BackgroundThread(activity);
        bt.parseOpmlForPodcasts(file, activity);
        
    }
}
