package com.fireminder.podcastcatcher.models;

import android.content.ContentValues;
import android.database.Cursor;

import com.fireminder.podcastcatcher.IvyApplication;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;
import com.fireminder.podcastcatcher.utils.Logger;
import com.fireminder.podcastcatcher.utils.PlaybackUtils;
import com.google.common.io.CharStreams;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

public class EpisodeModel {

  private static final String LOG_TAG = "EpisodeModel";

  public List<Episode> getEpisodes(String url) {
    List<Episode> episodes = null;
    try {
      final InputStream is = new URL(url).openConnection().getInputStream();
      final String feed = CharStreams.toString(new InputStreamReader(is, "UTF-8"));
      episodes = Episode.parseEpisodesFromResponse(feed);
    } catch (Exception e) {
      Logger.e(LOG_TAG, "url: " + url + " Exception: " + e.getMessage());
      e.printStackTrace();
    }
    return episodes;
  }

  public void bulkInsert(List<Episode> episodes, String mPodcastId) {
    ContentValues[] contentValues = new ContentValues[episodes.size()];
    for (int i = 0; i < episodes.size(); i++) {
      ContentValues cv = Episode.episodeToContentValues(episodes.get(i));
      cv.put(PodcastCatcherContract.Podcasts.PODCAST_ID, mPodcastId);
      contentValues[i] = cv;
    }

    IvyApplication.getAppContext().getContentResolver()
        .bulkInsert(PodcastCatcherContract.Episodes.CONTENT_URI, contentValues);

  }

}
