package com.fireminder.podcastcatcher.receiver;

import android.app.DownloadManager;
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
    String action;
    switch (intent.getAction()) {
      case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
        action = DownloadManagerService.ACTION_DOWNLOAD_COMPLETE;
        break;
      case DownloadManager.ACTION_NOTIFICATION_CLICKED:
        action = DownloadManagerService.ACTION_NOTIFICATION_CLICKED;
        break;
      default:
        throw new UnsupportedOperationException("Unknown action: " + intent.getAction());
    }

    Intent downloadCompleteIntent = new Intent(
        action,
        null, context, DownloadManagerService.class);
    downloadCompleteIntent.putExtras(intent.getExtras());
    context.startService(downloadCompleteIntent);
  }
}
