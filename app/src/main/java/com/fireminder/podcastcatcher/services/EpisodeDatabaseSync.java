package com.fireminder.podcastcatcher.services;

import android.app.IntentService;
import android.content.Intent;

public class EpisodeDatabaseSync extends IntentService {

  public EpisodeDatabaseSync() {
    super("EpisodeDataSync");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    if (intent != null) {
      // for episode in episodes
      // if episode.isDownloaded
      // if episode.localUri dne
      // episode.isDownloaded = false
    }
    throw new UnsupportedOperationException("Not yet implemented");
  }

}
