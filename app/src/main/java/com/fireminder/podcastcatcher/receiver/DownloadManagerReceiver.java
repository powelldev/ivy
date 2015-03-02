package com.fireminder.podcastcatcher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fireminder.podcastcatcher.services.DownloadManagerService;

public class DownloadManagerReceiver extends BroadcastReceiver {

  public DownloadManagerReceiver() {
  }

  /* Receiver to process DownloadManager completions */
  @Override
  public void onReceive(Context context, Intent intent) {
    Intent downloadCompleteIntent = new Intent(
        DownloadManagerService.ACTION_DOWNLOAD_COMPLETE,
        null, context, DownloadManagerService.class);
    downloadCompleteIntent.putExtras(intent.getExtras());
    context.startService(downloadCompleteIntent);
  }
}
