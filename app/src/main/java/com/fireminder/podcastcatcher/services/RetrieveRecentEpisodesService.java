package com.fireminder.podcastcatcher.services;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;

import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;

import java.util.ArrayList;
import java.util.List;


public class RetrieveRecentEpisodesService extends IntentService {
  private static final String LOG_TAG = RetrieveRecentEpisodesService.class.getSimpleName();

  public RetrieveRecentEpisodesService() {
    super("RetrieveEpisodeService");
  }


  @Override
  protected void onHandleIntent(Intent intent) {
    List<String> podcastIds = new ArrayList<>();
    Cursor cursor = getContentResolver().query(PodcastCatcherContract.Podcasts.CONTENT_URI,
        new String[] {PodcastCatcherContract.Podcasts.PODCAST_ID },
        null,
        null,
        null);
    while (cursor.moveToNext()) {
      String id = cursor.getString(0);
      podcastIds.add(id);
    }
    cursor.close();
//    throw new UnsupportedOperationException("Not yet implemented");
  }
}
