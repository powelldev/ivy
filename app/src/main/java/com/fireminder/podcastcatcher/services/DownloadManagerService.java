package com.fireminder.podcastcatcher.services;

import android.app.DownloadManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.text.TextUtils;

import com.fireminder.podcastcatcher.models.Episode;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;
import com.fireminder.podcastcatcher.utils.PrefUtils;
import com.fireminder.podcastcatcher.utils.Utils;

import java.io.File;

public class DownloadManagerService extends Service {

  public static final String ACTION_DOWNLOAD_COMPLETE = "action_download_complete";
  private static final String ACTION_START_DOWNLOAD = "action_start_download";
  private static final String EXTRA_EPISODE = "extra_episode";
  private static final String LOG_TAG = DownloadManagerService.class.getSimpleName();

  /**
   * Launches a download of the content at episode's streamUri. Only entry point to this service
   */
  public static void download(Context context, Episode episode) {
    Intent intent = new Intent(context, DownloadManagerService.class);
    intent.setAction(ACTION_START_DOWNLOAD);
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
        final String id = Long.toString(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1));
        downloadComplete(id);
        break;
      case ACTION_START_DOWNLOAD:
        final Episode episode = intent.getParcelableExtra(EXTRA_EPISODE);
        startDownload(episode);
        break;
      default:
        throw new UnsupportedOperationException("Unknown action: " + intent.getAction());
    }
  }

  private void startDownload(Episode episode) {
    if (isEpisodeAlreadyDownloaded(episode) || isEpisodeDownloading(episode)) {
      updateEpisodeAsDownloaded(episode);
      return;
    }

    DownloadManager dm = (DownloadManager) getApplicationContext().getSystemService(DOWNLOAD_SERVICE);
    DownloadManager.Request request = determineDownloadConfiguration(episode);

    final long downloadId = dm.enqueue(request);
    flagEpisodeAsDownloading(episode, downloadId);
  }

  private boolean isEpisodeDownloading(Episode episode) {
    // TODO: implement
    return false;
  }

  private void flagEpisodeAsDownloading(Episode episode, long downloadId) {
    final SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(Utils.DL_PREF, MODE_PRIVATE).edit();
    editor.putString(Long.toString(downloadId), episode.episodeId).apply();
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

  // TODO: Determien another way to store episode name other than episode id.
  private Uri getEpisodeUri(Episode episode) {
    return Utils.createEpisodeDestination(getApplicationContext(), episode.episodeId);
  }

  /**
   * Set the DownloadManager.Request object to conform ot user-defined preferences.
   * @param episode
   * @return
   */
  private DownloadManager.Request determineDownloadConfiguration(Episode episode) {
    final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(episode.streamUri));

    request.setDestinationUri(getEpisodeUri(episode));

    request.setAllowedNetworkTypes( PrefUtils.isMobileAllowed(getApplicationContext()) ?
        DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI :
        DownloadManager.Request.NETWORK_WIFI);

    return request;
  }

  private void updateEpisodeAsDownloaded(Episode episode) {
    episode.isDownloaded = true;
    episode.localUri = Utils.createEpisodeDestination(getApplicationContext(), episode.episodeId).toString();

    final ContentValues values = new ContentValues();
    values.put(PodcastCatcherContract.Episodes.EPISODE_IS_DOWNLOADED, episode.isDownloaded ? 1 : 0);
    values.put(PodcastCatcherContract.Episodes.EPISODE_LOCAL_URI, episode.localUri);
    getContentResolver().update(PodcastCatcherContract.Episodes.buildEpisodeUri(episode.episodeId), values, null, null);
  }


  /**
   * Persisting episode local uri and download status in the database.
   * @param id The episode id of the episode whose download has completed.
   */
  private void downloadComplete(String id) {
    //Get episode's id
    final SharedPreferences preferences = getApplicationContext().getSharedPreferences(Utils.DL_PREF, MODE_PRIVATE);
    final String episodeId = preferences.getString(id, "");

    if (TextUtils.isEmpty(episodeId)) {
      return;
    }

    //Get episode from Db
    final Cursor cursor = getApplicationContext().getContentResolver()
        .query(PodcastCatcherContract.Episodes.buildEpisodeUri(episodeId), null, null, null, null);
    cursor.moveToFirst();
    if (cursor.getCount() > 0) {
      final Episode episode = Episode.parseEpisodeFromCursor(cursor);
    cursor.close();

    updateEpisodeAsDownloaded(episode);
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
