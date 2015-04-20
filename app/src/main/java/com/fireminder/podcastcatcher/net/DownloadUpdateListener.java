package com.fireminder.podcastcatcher.net;

import com.fireminder.podcastcatcher.models.Episode;

public interface DownloadUpdateListener {

  void onProgress(Episode episode, long downloaded, long total);
  void onCompleted(Episode episode);
}
