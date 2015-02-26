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
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RetrieveEpisodeService extends IntentService {
  private static final String LOG_TAG = RetrieveEpisodeService.class.getSimpleName();

  private static final String ACTION_RETRIEVE_ALL = "com.fireminder.podcastcatcher.services.action.retrieve_all";
  private static final String EXTRA_PODCAST_ID = "com.fireminder.podcastcatcher.services.extra.podcastId";
  private static final String EXTRA_FEED_URL = "com.fireminder.podcastcatcher.services.extra.podcastFeed";

  String mPodcastId;
  String mFeedUrl;

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
    try {
      InputStream is = new URL(feedUrl).openConnection().getInputStream();
      String response = CharStreams.toString(new InputStreamReader(is, "UTF-8"));
      List<Episode> episodes = Episode.parseEpisodesFromResponse(response);
      ContentValues[] contentValues = new ContentValues[episodes.size()];
      for (int i = 0; i < episodes.size(); i++) {
        ContentValues cv = new ContentValues();
        cv.put(PodcastCatcherContract.Episodes.EPISODE_ID, episodes.get(i).episode_id);
        cv.put(PodcastCatcherContract.Podcasts.PODCAST_ID, mPodcastId);
        cv.put(PodcastCatcherContract.Episodes.EPISODE_TITLE, episodes.get(i).title);
        cv.put(PodcastCatcherContract.Episodes.EPISODE_DESCRIPTION, episodes.get(i).description);
        cv.put(PodcastCatcherContract.Episodes.EPISODE_STREAM_URI, episodes.get(i).stream_uri);
        cv.put(PodcastCatcherContract.Episodes.EPISODE_LOCAL_URI, episodes.get(i).local_uri);
        cv.put(PodcastCatcherContract.Episodes.EPISODE_PUBLICATION_DATE, episodes.get(i).pubDate);
        cv.put(PodcastCatcherContract.Episodes.EPISODE_CONTENT_DURATION, episodes.get(i).duration);
        cv.put(PodcastCatcherContract.Episodes.EPISODE_PERCENT_ELAPSED, episodes.get(i).elapsed);
        cv.put(PodcastCatcherContract.Episodes.EPISODE_IS_DOWNLOADED, episodes.get(i).isDownloaded ? 1 : 0);
        contentValues[i] = cv;
      }
      getContentResolver().bulkInsert(PodcastCatcherContract.Episodes.CONTENT_URI, contentValues);
    } catch (Exception exception) {
      Logger.e(LOG_TAG, "handleRetrieveAll( " + feedUrl + " ): error: " + exception.getMessage());
      exception.printStackTrace();
    }
  }

}
