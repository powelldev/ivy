package fireminder.podcastcatcher.activities;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class Searcher {

    OnSearchComplete listener;
    Context context;
    Handler handler = new Handler();

    static final String LOG_TAG = "PodcastCatcher";

    Searcher(Context context, OnSearchComplete listener) {
        this.context = context;
        this.listener = listener;
    }

    private String sanitizeString(String term) {
        try {
            term = URLEncoder.encode(term, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return term;
    }

    List<String> results;

    public void search(String term) {
        term = sanitizeString(term);
        final String searchUrl = String.format(
                "https://itunes.apple.com/search?media=podcast&term=%s&attribute=titleTerm", term);

        new Thread((new Runnable() {
            public void run() {
                results = performSearchRequest(searchUrl);
                Handler contextHandler = new Handler(context.getMainLooper());
                contextHandler.post(reportResultRunnable);
            }
        })).start();

    }

    private Runnable reportResultRunnable = new Runnable() {
        public void run() {
            if (results != null) {
                listener.searchComplete(results);
            } else {
                listener.searchFailure();
            }
        }
    };

    private List<String> performSearchRequest(String urlStr) {
        try {
            Log.d(LOG_TAG, "Connecting to: " + urlStr);
            URL url = new URL(urlStr);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setConnectTimeout(10000);
            InputStream is = urlConn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            List<String> results = parseJsonForPodcasts(reader);
            return results;
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
            return null;
        }
    }

    private List<String> parseJsonForPodcasts(BufferedReader reader) {
        Log.d(LOG_TAG, "Parsing from reader");
        try {
            JSONObject jsonSearchResults = convertReaderToJsonObject(reader);

            List<String> itunesStringResults = new ArrayList<String>();
            JSONArray podcast = jsonSearchResults.getJSONArray("results");
            itunesStringResults.addAll(parseJsonArrayIntoStrings(podcast));
            return itunesStringResults;
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
            return null;
        }
    }

    List<String> parseJsonArrayIntoStrings(JSONArray jsonArray) throws JSONException {
        Log.d(LOG_TAG, "Parsing json into strings");
        List<String> strings = new ArrayList<String>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            strings.add(obj.getString(ItunesPodcastResult.NAME));
            strings.add(obj.getString(ItunesPodcastResult.ARTIST));
            strings.add(obj.getString(ItunesPodcastResult.FEED_URL));
        }
        return strings;
    }

    public JSONObject convertReaderToJsonObject(BufferedReader reader) {
        try {
            StringBuilder sb = new StringBuilder();
            String line = null;
            JSONObject jsonObj = null;
            String json = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            json = sb.toString();
            jsonObj = new JSONObject(json);
            return jsonObj;
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
            return null;
        }
    }

    private static class ItunesPodcastResult {
        static final String NAME = "collectionName";
        static final String ARTIST = "artistName";
        static final String FEED_URL = "feedUrl";
        public static String[] COLUMNS = new String[] { NAME, ARTIST, FEED_URL };

        public static String[] getColumns() {
            return COLUMNS;
        }

        static final String IMAGE_URL_30 = "artworkUrl30";
        static final String IMAGE_URL_60 = "artworkUrl60";
        static final String IMAGE_URL_100 = "artworkUrl100";
    }

    public interface OnSearchComplete {
        public void searchComplete(List<String> results);

        public void searchFailure();
    }
}
