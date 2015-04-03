package com.fireminder.podcastcatcher.net;

import android.net.Uri;

public class DownloadTask {
  public final int id;
  public final Uri uri;
  public final FileType type;

  public DownloadTask(int id, Uri uri, FileType type) {
    this.id = id;
    this.uri = uri;
    this.type = type;
  }
}
