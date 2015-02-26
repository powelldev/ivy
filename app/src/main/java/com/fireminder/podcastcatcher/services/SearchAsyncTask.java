package com.fireminder.podcastcatcher.services;

import android.net.Uri;
import android.os.AsyncTask;

import com.fireminder.podcastcatcher.utils.Logger;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class SearchAsyncTask extends AsyncTask<Void, Void, List<SearchAsyncTask.SearchResult>> {
  private static final String LOG_TAG = SearchAsyncTask.class.getSimpleName();

  private SearchListener mCallback;
  private String mSearchTerm;

  public SearchAsyncTask(String searchTerm, SearchListener callback) {
    mSearchTerm = searchTerm;
    mCallback = callback;
  }

  @Override
  protected List<SearchResult> doInBackground(Void... params) {
    return search(mSearchTerm);
  }

  @Override
  protected void onPostExecute(List<SearchResult> results) {
    mCallback.onSearchComplete(results);
  }

  public interface SearchListener {
    void onSearchComplete(List<SearchResult> searchResults);
  }


  private String sanitizeString(String term) {
    try {
      term = URLEncoder.encode(term, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return term;
  }

  public List<SearchResult> search(String searchTerm) {
    List<SearchResult> searchResults;
    try {
      final Uri uri = Uri.parse("https://itunes.apple.com/search").buildUpon()
          .appendQueryParameter("media", "podcast")
          .appendQueryParameter("term", searchTerm)
          .appendQueryParameter("attribute", "titleTerm").build();
      final URL url = new URL(uri.toString());
      final InputStream is = url.openConnection().getInputStream();
      final String response = CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
      JSONObject jsonObj = new JSONObject(response);
      JSONArray results = jsonObj.getJSONArray("results");
      searchResults = new ArrayList<>(results.length());
      for (int i = 0; i < results.length(); i++) {
        SearchResult searchResult = parseSearchResult((JSONObject) results.get(i));
        searchResults.add(searchResult);
      }
    } catch (Exception e) {
      Logger.e(LOG_TAG, e.getMessage());
      e.printStackTrace();
      return null;
    }
    return searchResults;
  }

  private SearchResult parseSearchResult(JSONObject result) {
    try {
      SearchResult searchResult = new SearchResult();
      searchResult.title = result.getString(ItunesApi.NAME);
      searchResult.feedUrl = result.getString(ItunesApi.FEED_URL);
      searchResult.imageUri = result.getString(ItunesApi.IMAGE_URL_60);
      return searchResult;
    } catch (Exception e) {
      Logger.e(LOG_TAG, "parseSearchResult Error: " + e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  public class SearchResult {
    public String title;
    public String feedUrl;
    public String imageUri;
  }

  private static class ItunesApi {
    static final String NAME = "collectionName";
    static final String ARTIST = "artistName";
    static final String FEED_URL = "feedUrl";
    public static String[] COLUMNS = new String[]{NAME, ARTIST, FEED_URL};

    public static String[] getColumns() {
      return COLUMNS;
    }

    static final String IMAGE_URL_30 = "artworkUrl30";
    static final String IMAGE_URL_60 = "artworkUrl60";
    static final String IMAGE_URL_100 = "artworkUrl100";
  }

}
