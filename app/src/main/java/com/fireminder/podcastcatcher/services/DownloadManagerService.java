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
import android.util.Log;

import com.fireminder.podcastcatcher.models.Episode;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;
import com.fireminder.podcastcatcher.utils.Logger;
import com.fireminder.podcastcatcher.utils.Utils;

public class DownloadManagerService extends Service {

  public static final String ACTION_DOWNLOAD_COMPLETE = "action_download_complete";
  public static final String ACTION_START_DOWNLOAD = "action_start_download";
  public static final String EXTRA_EPISODE = "extra_episode";
  private static final String LOG_TAG = DownloadManagerService.class.getSimpleName();

  public static void download(Context context, Episode episode) {
    Intent intent = new Intent(context, DownloadManagerService.class);
    intent.setAction(ACTION_START_DOWNLOAD);
    intent.putExtra(EXTRA_EPISODE, episode);
    context.startService(intent);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null && intent.getAction() != null) {
      Episode episode;
      switch (intent.getAction()) {
        case ACTION_DOWNLOAD_COMPLETE:
          String id = Long.toString(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1));
          Log.e(LOG_TAG, "Download Id Complete: " + id);
          SharedPreferences preferences = getApplicationContext().getSharedPreferences(Utils.DL_PREF, MODE_PRIVATE);
          String episodeId = preferences.getString(id, "");
          Log.e(LOG_TAG, "Download Id Complete episode : " + episodeId);
          Cursor cursor = getApplicationContext().getContentResolver().query(PodcastCatcherContract.Episodes.buildEpisodeUri(episodeId), null, null, null, null);
          cursor.moveToFirst();
          try {
            episode = Episode.parseEpisodeFromCursor(cursor);
            episode.isDownloaded = true;
            episode.local_uri = Uri.withAppendedPath(Uri.fromFile(getApplicationContext().getExternalFilesDir(null)),
                episode.episode_id).toString();
            ContentValues values = new ContentValues();
            values.put(PodcastCatcherContract.Episodes.EPISODE_IS_DOWNLOADED, episode.isDownloaded ? 1 : 0);
            values.put(PodcastCatcherContract.Episodes.EPISODE_LOCAL_URI, episode.local_uri);
            getContentResolver().update(PodcastCatcherContract.Episodes.buildEpisodeUri(episodeId), values, null, null);

          } catch (Exception e) {
            Logger.e(LOG_TAG, "download failed");
            e.printStackTrace();
          }

          break;
        case ACTION_START_DOWNLOAD:
          episode = intent.getParcelableExtra(EXTRA_EPISODE);
          final DownloadManager dm = (DownloadManager) getApplicationContext().getSystemService(DOWNLOAD_SERVICE);
          final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(episode.stream_uri));
          Uri uri = Uri.withAppendedPath(Uri.fromFile(getApplicationContext().getExternalFilesDir(null)),
              episode.episode_id);
          Log.e(LOG_TAG, "Download Location: " + uri.toString());
          request.setDestinationUri(uri);
          long downloadId = dm.enqueue(request);
          Log.e(LOG_TAG, "Download Id: " + downloadId);
          final SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(Utils.DL_PREF, MODE_PRIVATE).edit();
          editor.putString(Long.toString(downloadId), episode.episode_id);
          editor.apply();
          break;
      }
    }
    return START_NOT_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
