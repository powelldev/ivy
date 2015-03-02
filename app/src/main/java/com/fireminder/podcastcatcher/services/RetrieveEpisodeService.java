package com.fireminder.podcastcatcher.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import com.fireminder.podcastcatcher.models.Episode;
import com.fireminder.podcastcatcher.models.Podcast;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;
import com.fireminder.podcastcatcher.utils.Logger;
import com.google.common.io.CharStreams;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

/**
 * Background service for parsing all the episodes from a podcast. Episodes are
 * automatically updated with podcastId and inserted into the database.
 */
public class RetrieveEpisodeService extends IntentService {

  private static final String LOG_TAG = RetrieveEpisodeService.class.getSimpleName();

  private static final String ACTION_RETRIEVE_ALL = "com.fireminder.podcastcatcher.services.action.retrieve_all";
  private static final String EXTRA_PODCAST_ID = "com.fireminder.podcastcatcher.services.extra.podcastId";
  private static final String EXTRA_FEED_URL = "com.fireminder.podcastcatcher.services.extra.podcastFeed";

  private String mPodcastId;
  private String mFeedUrl;

  public static void startActionRetrieveAll(Context context, Podcast podcast) {
    Intent intent = new Intent(context, RetrieveEpisodeService.class);
    intent.setAction(ACTION_RETRIEVE_ALL);
    intent.putExtra(EXTRA_PODCAST_ID, podcast.podcastId);
    intent.putExtra(EXTRA_FEED_URL, podcast.feed);
    context.startService(intent);
  }

  public RetrieveEpisodeService() {
    super("RetrieveEpisodeService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    if (intent != null) {
      final String action = intent.getAction();
      if (ACTION_RETRIEVE_ALL.equals(action)) {
        mPodcastId = intent.getStringExtra(EXTRA_PODCAST_ID);
        mFeedUrl = intent.getStringExtra(EXTRA_FEED_URL);
        handleRetrieveAll(mFeedUrl);
      }
    }
  }

  private void handleRetrieveAll(String feedUrl) {
    final List<Episode> episodes = getEpisodes(feedUrl);
    if (episodes != null) {
      ContentValues[] contentValues = new ContentValues[episodes.size()];
      for (int i = 0; i < episodes.size(); i++) {
        ContentValues cv = Episode.episodeToContentValues(episodes.get(i));
        cv.put(PodcastCatcherContract.Podcasts.PODCAST_ID, mPodcastId);
        contentValues[i] = cv;
      }
      getContentResolver().bulkInsert(PodcastCatcherContract.Episodes.CONTENT_URI, contentValues);
    }
  }

  private List<Episode> getEpisodes(String url) {
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

}
