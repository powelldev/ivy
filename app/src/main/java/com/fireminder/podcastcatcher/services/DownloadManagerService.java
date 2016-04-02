package com.fireminder.podcastcatcher.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;

import com.fireminder.podcastcatcher.IvyApplication;
import com.fireminder.podcastcatcher.models.Episode;
import com.fireminder.podcastcatcher.models.EpisodeModel;
import com.fireminder.podcastcatcher.net.BigNotificationManager;
import com.fireminder.podcastcatcher.net.DownloadManager;
import com.fireminder.podcastcatcher.utils.Logger;

import java.io.File;

import javax.inject.Inject;

public class DownloadManagerService extends Service {

  private static final String TAG = "DownloadManagerService";

  @Inject
  DownloadManager downloadManager;

  @Inject
  EpisodeModel episodeModel;

  private BigNotificationManager notifyManager;

  public static final String ACTION_NOTIFICATION_CLICKED = "action_notification_clicked";
  private static final String ACTION_START_DOWNLOAD = "action_start_download";
  private static final String EXTRA_EPISODE = "extra_episode";
  private static final String LOG_TAG = DownloadManagerService.class.getSimpleName();

  /**
   * Launches a download of the content at episode's streamUri.
   */
  public static void download(Context context, Episode episode) {
    Intent intent = new Intent(context, DownloadManagerService.class);
    intent.setAction(ACTION_START_DOWNLOAD);
    intent.putExtra(EXTRA_EPISODE, episode);
    context.startService(intent);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    IvyApplication.getAppContext().getDbComponent().inject(this);
    if (intent != null && intent.getAction() != null) {
      String command = intent.getAction();
      Logger.i(TAG, "onStartCommand(), intent action: " + command);
      handleCommand(intent.getAction(), intent.getExtras());
    }
    return START_NOT_STICKY;
  }

  private void handleCommand(String command, Bundle extras) {
    switch (command) {
      case ACTION_START_DOWNLOAD:
        final Episode episode = extras.getParcelable(EXTRA_EPISODE);
        notifyManager = BigNotificationManager.getInstance(IvyApplication.getAppContext());
        download(episode);
        break;
      case ACTION_NOTIFICATION_CLICKED:
        Logger.assertOrError(TAG, "Notification click not implemented");
        break;
      default:
        throw new UnsupportedOperationException("Unknown action: " + command);
    }
  }

  public void download(final Episode episode) {

    notifyManager.addEpisode(episode);

    new AsyncTask<Void, Void, Void> () {

      @Override
      protected Void doInBackground(Void... params) {
        downloadManager.download(episode, new DownloadManager.DownloadCallback() {
          @Override
          public void onProgress(long downloaded, long total) {
            notifyManager.onProgress(episode, downloaded, total);
          }

          @Override
          public void onCompleted(Exception e, File file) {
            if (e != null) {
              Logger.e(TAG, "onComplete failed: ", e);
            }
            notifyManager.onCompleted(episode);
            episodeModel.updateDownloadedStatus(episode, Episode.DownloadStatus.DOWNLOADED, Uri.parse(file.getAbsolutePath()));
          }


          @Override
          public void onError(Exception e) {
            Logger.e(TAG, "onComplete failed: ", e);
            notifyManager.onCompleted(episode);
            episodeModel.updateDownloadedStatus(episode, Episode.DownloadStatus.DOWNLOAD_INTERRUPTED, null);
          }

          @Override
          public void onNoInternet() {
            throw new IllegalArgumentException("Not yet implemented");
          }

        });
        return null;
      }
    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
