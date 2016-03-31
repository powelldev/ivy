package com.fireminder.podcastcatcher.net;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.models.Episode;
import com.fireminder.podcastcatcher.services.DownloadManagerService;

import java.util.HashMap;

/**
 * Class responsible for showing/merging all downloads and their update status as a single notification.
 */
public class BigNotificationManager implements DownloadUpdateListener {

  private Context context;
  private NotificationManager mNotificationManager;

  private HashMap<Episode, Double> progressMap = new HashMap<>();

  private static BigNotificationManager instance;
  public static BigNotificationManager getInstance(Context context) {
    if (instance == null) {
      instance = new BigNotificationManager(context);
    }
    return instance;
  }

  private BigNotificationManager(Context context) {
    mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    this.context = context.getApplicationContext();
    progressMap = new HashMap<>();
  }

  private static final int NOTIFICATION_ID = 1337;

  public void addEpisode(Episode episode) {
    progressMap.put(episode, 0d);
    Notification note = createNotification(episode);
    mNotificationManager.notify( NOTIFICATION_ID, note );
  }

  private Notification createNotification(@Nullable Episode episode) {
    NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
    builder.setSmallIcon(R.drawable.ic_white_icon);
    int size = progressMap.size();
    if (size == 1) {
      Episode item = progressMap.keySet().iterator().next();
      double progress = progressMap.get(item);
      builder.setContentTitle(episode.title)
          .setContentText(episode.description)
          .setProgress(100, (int) progress*100, false);

    } else {
      builder.setContentTitle( String.format("Downloading %d episodes", size) );
      builder.setContentText(" ");
    }
    return builder.build();
  }


  @Override
  public void onProgress(Episode episode, long downloaded, long total) {
    double progress = toProgress(downloaded, total);
    progressMap.put(episode, progress);
    Notification note = createNotification(episode);
    if (!spamFilter.active()) {
      mNotificationManager.notify(NOTIFICATION_ID, note);
    }
  }

  SpamFilter spamFilter = new SpamFilter();

  private double toProgress(Long downloaded, Long total) {
    return (downloaded.doubleValue() / total.doubleValue());
  }

  @Override
  public void onCompleted(Episode episode) {
    progressMap.remove(episode);
    mNotificationManager.cancel(NOTIFICATION_ID);
    DownloadManagerService.downloadHasCompleted(context, episode);
  }

  private class SpamFilter {
    Handler handler = new Handler(Looper.getMainLooper());
    boolean active = false;

    public boolean active() {
      if (!active) {
        handler.postDelayed(new Runnable() {
          @Override
          public void run() {
            active = false;
          }
        }, 1000);
        active = true;
      }
      return active;
    }
  }
}

