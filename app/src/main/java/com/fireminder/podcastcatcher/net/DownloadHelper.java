package com.fireminder.podcastcatcher.net;

import android.net.Uri;

import com.fireminder.podcastcatcher.models.Episode;
import com.fireminder.podcastcatcher.models.Podcast;

import java.util.List;

public class DownloadHelper {
  private final DownloadQueueProcessor processor;
  private List<DownloadTask> downloadTaskList;
  private int id = 0;

  public DownloadHelper(DownloadQueueProcessor processor) {
    this.processor = processor;
  }

  public void download(Episode episode) {
    DownloadTask task = new DownloadTask(id, Uri.parse(episode.localUri), FileType.MP3);
    processor.enqueue(task);
    processor.process();
  }
}
