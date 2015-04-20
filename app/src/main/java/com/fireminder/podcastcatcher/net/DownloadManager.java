package com.fireminder.podcastcatcher.net;

import android.content.Context;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.models.Episode;
import com.fireminder.podcastcatcher.utils.Utils;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import java.io.File;

public class DownloadManager {

  private final Context mContext;
  private NotificationCompat.Builder builder;
  private BigNotificationManager notifyManager;

  public static final String GROUP_DOWNLOADS = "group_downloads";

  public DownloadManager(Context context) {
    mContext = context.getApplicationContext();
    notifyManager = BigNotificationManager.getInstance(context);
  }


  public void download(final Episode episode) {
    if (isEpisodeAlreadyDownloaded(episode)) {
      return;
    }
    builder = episodeToBuilder(episode);

    Uri uri = Utils.createEpisodeDestination(mContext, episode.episodeId);
    File destination = new File(uri.getPath());

    notifyManager.addEpisode(episode);

    Ion.with(mContext)
        .load(episode.streamUri)
        .progress(new ProgressCallback() {

          @Override
          public void onProgress(long downloaded, long total) {
            notifyManager.onProgress(episode, downloaded, total);
          }
        })
        .write(destination)
        .setCallback(new FutureCallback<File>() {
          @Override
          public void onCompleted(Exception e, File result) {
            notifyManager.onCompleted(episode);
          }
        });
  }

  private NotificationCompat.Builder episodeToBuilder(Episode episode) {
    NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
    builder.setContentTitle(episode.title)
        .setContentText(episode.description)
        .setSmallIcon(R.drawable.ic_white_icon);
    return builder;
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
    return Utils.createEpisodeDestination(mContext, episode.episodeId);
  }
}
