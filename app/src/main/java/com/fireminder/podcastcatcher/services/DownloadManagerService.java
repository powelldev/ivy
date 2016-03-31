package com.fireminder.podcastcatcher.services;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

import com.fireminder.podcastcatcher.models.Episode;
import com.fireminder.podcastcatcher.net.DownloadManager;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;
import com.fireminder.podcastcatcher.utils.Utils;

import java.io.File;

public class DownloadManagerService extends Service {

  public static final String ACTION_DOWNLOAD_COMPLETE = "action_download_complete";
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

  public static void downloadHasCompleted(Context context, Episode episode) {
    Intent intent = new Intent(context, DownloadManagerService.class);
    intent.setAction(ACTION_DOWNLOAD_COMPLETE);
    intent.putExtra(EXTRA_EPISODE, episode);
    context.startService(intent);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null && intent.getAction() != null) {
      handleCommand(intent);
    }
    return START_NOT_STICKY;
  }

  private void handleCommand(Intent intent) {
    switch (intent.getAction()) {
      case ACTION_DOWNLOAD_COMPLETE:
        final Episode episodeT = intent.getParcelableExtra(EXTRA_EPISODE);
        updateEpisodeAsDownloaded(episodeT);
        break;
      case ACTION_START_DOWNLOAD:
        final Episode episode = intent.getParcelableExtra(EXTRA_EPISODE);
        startDownload(episode);
        break;
      case ACTION_NOTIFICATION_CLICKED:
        break;
      default:
        throw new UnsupportedOperationException("Unknown action: " + intent.getAction());
    }
  }

  private void startDownload(Episode episode) {
    if (isEpisodeAlreadyDownloaded(episode)) {
      updateEpisodeAsDownloaded(episode);
      return;
    }

    DownloadManager dm = new DownloadManager(this);
    dm.download(episode);
  }

  /**
   * Checks if the episode's uri already has a file.
   * @param episode
   * @return
   */
  private boolean isEpisodeAlreadyDownloaded(Episode episode) {
    Uri uri = getEpisodeUri(episode);
    File file = new File(uri.getPath());
    return file.exists();
  }
  // TODO: Determine another way to store episode name other than episode id.
  private Uri getEpisodeUri(Episode episode) {
    return Utils.createEpisodeDestination(getApplicationContext(), episode.episodeId);
  }

  private void updateEpisodeAsDownloaded(Episode episode) {
    episode.isDownloaded = true;
    episode.localUri = Utils.createEpisodeDestination(getApplicationContext(), episode.episodeId).toString();

    final ContentValues values = new ContentValues();
    values.put(PodcastCatcherContract.Episodes.EPISODE_IS_DOWNLOADED, episode.isDownloaded ? 1 : 0);
    values.put(PodcastCatcherContract.Episodes.EPISODE_LOCAL_URI, episode.localUri);
    getContentResolver().update(PodcastCatcherContract.Episodes.buildEpisodeUri(episode.episodeId), values, null, null);
  }


  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
