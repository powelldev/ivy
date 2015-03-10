package com.fireminder.podcastcatcher.mediaplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.models.Episode;
import com.fireminder.podcastcatcher.models.Podcast;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;
import com.fireminder.podcastcatcher.ui.activities.PodcastsActivity;
import com.fireminder.podcastcatcher.utils.PlaybackUtils;
import com.fireminder.podcastcatcher.utils.PrefUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaPlayerService extends Service implements StatefulMediaPlayer.MediaStateListener {

  private static final String LOG_TAG = MediaPlayerService.class.getSimpleName();

  private static final int SKIP_TIME_MILLIS = 30 * 1000;
  private static final int NO_ARG = -1;

  // Actions to perform media controls
  public static final String ACTION_PLAY = "action_play";
  public static final String ACTION_SKIP = "action_skip";
  public static final String ACTION_RESUME = "action_resume";
  public static final String ACTION_PAUSE_TOGGLE = "action_pause_toggle";
  public static final String ACTION_STOP = "action_stop";

  // Action to determine if we shut down this service when the user exits the app
  // if we're playing, stay alive, if we're paused, exit
  // if headphones are taken out while we're paused, exit
  public static final String ACTION_LEAVING = "action_leaving";

  // Extras for communicating episode data
  public static final String EXTRA_MEDIA = "extra_item_to_play";
  public static final String EXTRA_MEDIA_CONTENT = "extra_media_content";

  // Message for adding the a caller's messenger to our list of clients.
  public static final int MSG_ADD_CLIENT = 100;

  public static final int MSG_START = 1000;

  // Flags for communication between client view controls and
  // the MediaPlayer
  public static final int MSG_SET_DATA = 200;
  public static final int MSG_PLAY_PAUSE = 300;
  public static final int MSG_SEEK_START = 400;
  public static final int MSG_SEEK_END = 401;
  public static final int MSG_BACK_THIRTY = 500;
  public static final int MSG_FORWARD_THIRTY = 501;
  public static final int MSG_NEXT = 502;
  public static final int MSG_PREVIOUS = 503;


  // Flags for communication from the MediaPlayer to our clients
  public static final int MSG_MEDIA_COMPLETE = 201;
  public static final int MSG_MEDIA_DURATION = 600;
  public static final int MSG_MEDIA_ELAPSED = 601;
  public static final int MSG_MEDIA_TITLE = 602; 
  public static final int MSG_HANDSHAKE_WITH_VIEW = 700; // Flag to send duration, elapsed, album art, and episode data to the view to be updated 
  public static final int MSG_NOTHING_PLAYING = 701; // Flag to send duration, elapsed, album art, and episode data to the view to be updated


  // TODO this could be better named
  private final StatefulMediaPlayer mediaPlayer = new StatefulMediaPlayer(this);

  // TODO this could be better named.
  private final Handler myHandler = new Handler(new Handler.Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      switch (msg.what) {
        case MSG_ADD_CLIENT:
          clients.add(msg.replyTo);
          break;
        case MSG_SET_DATA:
          final Episode media = msg.getData().getParcelable(EXTRA_MEDIA);
          setData(media, true);
          break;
        case MSG_START:
          start();
          break;
        case MSG_PLAY_PAUSE:
          // if we are returning from having closed the application, load
          // the last playing episode and begin playback, otherwise toggle play/pause
          String episodeId = PrefUtils.getEpisodePlaying(getApplicationContext());
          if (!TextUtils.isEmpty(episodeId) && mediaPlayer.getMedia() == null) {
            Cursor cursor = getApplicationContext().getContentResolver().query(PodcastCatcherContract.Episodes.buildEpisodeUri(episodeId), null, null, null, null);
            cursor.moveToFirst();
            setData(Episode.parseEpisodeFromCursor(cursor), true);
          } else {
            playPause();
          }
          break;
        case MSG_SEEK_START:
          seekStart();
          break;
        case MSG_SEEK_END:
          seekEnd(msg.arg1);
          break;
        case MSG_BACK_THIRTY:
          seekEnd((int) mediaPlayer.getCurrentPosition() - (SKIP_TIME_MILLIS));
          break;
        case MSG_FORWARD_THIRTY:
          seekEnd((int) mediaPlayer.getCurrentPosition() + (SKIP_TIME_MILLIS));
          break;
        case MSG_NEXT:
          next();
          break;
        case MSG_PREVIOUS:
          previous();
          break;
        case MSG_HANDSHAKE_WITH_VIEW:
          if (mediaPlayer.isPlaying()) {
            renamethis(true); // do not wish to reset view in this case
          }
          break;
        default:
          throw new UnsupportedOperationException("Unknown msg: " + msg.what);
      }
      return true;
    }
  });

  private void performViewHandshake() {
    if (mediaPlayer.isPlaying()) {
        int duration = (int) mediaPlayer.getDuration(getApplicationContext());
        sendMessage(MSG_MEDIA_DURATION, duration);
        mTimeElapsedHandler.removeCallbacks(postElapsedRunnable);
        mTimeElapsedHandler.post(postElapsedRunnable);
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_MEDIA, mediaPlayer.getMedia());
        bundle.putString(EXTRA_MEDIA_CONTENT, PlaybackUtils.getEpisodeImage(getApplicationContext(), mediaPlayer.getMedia()));
        sendMessage(MSG_HANDSHAKE_WITH_VIEW, (int) mediaPlayer.getCurrentPosition(), (int) mediaPlayer.getDuration(getApplicationContext()), bundle);
      }
  }

  private final List<Messenger> clients = new ArrayList<>();
  private final Messenger mMessenger = new Messenger(myHandler);
  private final Handler mTimeElapsedHandler = new Handler();

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null && intent.getAction() != null) {
      switch (intent.getAction()) {
        case ACTION_PLAY:
        case ACTION_RESUME:
          Episode media = intent.getParcelableExtra(EXTRA_MEDIA);
          setData(media, true);
          break;
        case ACTION_SKIP:
          next();
          break;
        case ACTION_PAUSE_TOGGLE:
          playPause();
          break;
        case ACTION_STOP:
          stop();
          break;
        case ACTION_LEAVING:
          sendInfoMessage("Leaving... ");
          if (mediaPlayer.mState != StatefulMediaPlayer.State.STARTED) {
            stopForeground(true);
            sendInfoMessage("service stopped");
          }
          break;
        default:
          throw new UnsupportedOperationException("Unsupported action: " + intent.getAction());
      }
    }
    return START_STICKY;
  }

  private void previous() {
    Episode current = mediaPlayer.getMedia();

    // Play next or stop playing
    Podcast podcast = PlaybackUtils.getPodcastOf(getApplicationContext(), current);
    Episode previous = PlaybackUtils.getPreviousEpisode(getApplicationContext(), podcast, current);

    if (previous != null) {
      PlaybackUtils.setEpisodeComplete(getApplicationContext(), previous, false);
      setData(previous, true);
    }
  }

  private void next() {
    onMediaCompleted();
  }

  private void start() {
    try {
      mediaPlayer.start();
    } catch (IOException e) {
      sendErrorMessage(e.getMessage());
      e.printStackTrace();
    }
  }

  private void playPause() {
    try {
      mediaPlayer.playPause();
    } catch (IllegalStateException e) {
      sendErrorMessage(e.getMessage());
      e.printStackTrace();
    }
  }

  private void seekStart() {
    mediaPlayer.seekStart();
  }

  private void stop() {
    try {
      mediaPlayer.stop();
    } catch (IllegalStateException e) {
      sendErrorMessage(e.getMessage());
      e.printStackTrace();
    }
  }


  private void setData(Episode media, boolean beginPlaybackImmediately) {
    try {
      mediaPlayer.setDataSource(media, beginPlaybackImmediately);
      Notification notification = setupNotification(media);
      startForeground(1, notification);
      Podcast podcast = PlaybackUtils.getPodcastOf(getApplicationContext(), media);
      int prefetch = PrefUtils.getNumEpisodesToPrefetch(getApplicationContext());
      PrefUtils.setEpisodePlaying(this, media.episodeId);
      PlaybackUtils.downloadNextXEpisodes(getApplicationContext(), podcast, prefetch);
    } catch (IOException e) {
      Toast.makeText(getApplicationContext(), "Out of episodes", Toast.LENGTH_SHORT).show();
      e.printStackTrace();
    }
  }

  /* Create icons and pending intents associated with this Service's notification */
  private Notification setupNotification(Episode episode) {
    Intent mainActivityIntent = new Intent(this, PodcastsActivity.class);
    Intent pauseToggleIntent = new Intent(this, MediaPlayerService.class).setAction(ACTION_PAUSE_TOGGLE);
    Intent skipIntent = new Intent(this, MediaPlayerService.class).setAction(ACTION_SKIP);

    PendingIntent pIntent = PendingIntent.getActivity(
        getApplicationContext(),
        0,
        mainActivityIntent,
        PendingIntent.FLAG_UPDATE_CURRENT);

    PendingIntent pauseTogglePendingIntent = PendingIntent.getService(
        getApplicationContext(),
        0,
        pauseToggleIntent,
        PendingIntent.FLAG_UPDATE_CURRENT);

    PendingIntent skipPendingIntent = PendingIntent.getService(
        getApplicationContext(),
        0,
        skipIntent,
        PendingIntent.FLAG_UPDATE_CURRENT);

    return new NotificationCompat.Builder(getApplicationContext())
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentText(episode.description)
        .setContentTitle(episode.title)
        .setPriority(Integer.MAX_VALUE)
        .setWhen(0)
        .setContentIntent(pIntent)
        .addAction(R.drawable.ic_play_arrow_white_48dp, "test", pauseTogglePendingIntent)
        .addAction(R.drawable.ic_skip_next_white_48dp, "test", skipPendingIntent)
        .build();
  }

  private void seekEnd(int position) {
    mediaPlayer.seekTo(position);
  }

  private void sendInfoMessage(String message) {
    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
  }

  private void sendErrorMessage(String message) {
    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
  }

  @Override
  public IBinder onBind(Intent intent) {
    return mMessenger.getBinder();
  }

  private void renamethis(boolean isPlaying) {
    if (isPlaying) {
      int duration = (int) mediaPlayer.getDuration(getApplicationContext());
      sendMessage(MSG_MEDIA_DURATION, duration);
      mTimeElapsedHandler.removeCallbacks(postElapsedRunnable);
      mTimeElapsedHandler.post(postElapsedRunnable);
      Bundle bundle = new Bundle();
      bundle.putParcelable(EXTRA_MEDIA, mediaPlayer.getMedia());
      bundle.putString(EXTRA_MEDIA_CONTENT, PlaybackUtils.getEpisodeImage(getApplicationContext(), mediaPlayer.getMedia()));
      sendMessage(MSG_HANDSHAKE_WITH_VIEW, (int) mediaPlayer.getCurrentPosition(), duration, bundle);
    } else {
     sendMessage(MSG_NOTHING_PLAYING);
    }
  }

  /*
  Here we should react to changes in the media player. Because the media player only utilizes this listener to
  communicate, it is inappropriate to react in other places.
   */
  @Override
  public void onStateUpdated(StatefulMediaPlayer.State state) {
    sendInfoMessage("State Change: " + state.name());
    switch (state) {
      case STARTED:
          renamethis(true);
        PrefUtils.setEpisodePlaying(getApplicationContext(), mediaPlayer.getMedia().episodeId);
        PrefUtils.setPodcastPlaying(getApplicationContext(), mediaPlayer.getMedia().podcastId);
        Notification notification = setupNotification(mediaPlayer.getMedia());
        startForeground(1, notification);
        break;
      case PREPARED:
        if (mediaPlayer.isPlaying()) {
            renamethis(true);
        }
        break;
      case PAUSED:
        PlaybackUtils.updateEpisodeElapsed(getApplicationContext(),
            mediaPlayer.getMedia(), mediaPlayer.getCurrentPosition());
        break;
      case COMPLETED:
        onMediaCompleted();
        break;
      case STOPPED:
        renamethis(false);
        break;
    }
  }

  private void onMediaCompleted() {
    Episode completed = mediaPlayer.getMedia();
    // Update View
    Bundle bundle = new Bundle();
    bundle.putParcelable(MediaPlayerService.EXTRA_MEDIA, completed);
    sendMessage(MSG_MEDIA_COMPLETE, -1, bundle);

    // Set Episode as complete
    PlaybackUtils.setEpisodeComplete(getApplicationContext(), completed, true);

    // Play next or stop playing
    Podcast podcast = PlaybackUtils.getPodcastOf(getApplicationContext(), completed);
    Episode next = PlaybackUtils.getNextEpisode(getApplicationContext(), podcast);

    if (next == null) {
      stopForeground(true);
    } else {
      setData(next, true);
      // TODO Delete old episode if configured that way.
    }
  }

  @Override
  public void onMediaElapsed(Episode media) {
    if (media != null) {
      int elapsed = (int) media.elapsed;
      sendMessage(MSG_MEDIA_ELAPSED, elapsed);
    }
  }


  /**
   * Wrapper for sending messages to all clients.
   *
   * @param what   argument for Message object
   * @param arg1   argument for Message object
   * @param bundle optional argument for data to pass to messenger
   */
  private void sendMessage(int what, int arg1, Bundle bundle) {
    for (Messenger messenger : clients) {
      try {
        Message msg = Message.obtain(null, what);
        msg.arg1 = arg1;
        if (bundle != null) {
          msg.setData(bundle);
        }
        messenger.send(msg);
      } catch (RemoteException e) {
        clients.remove(messenger);
      }
    }
  }

  private void sendMessage(int what, int arg1, int arg2, Bundle bundle) {
    for (Messenger messenger : clients) {
      try {
        Message msg = Message.obtain(null, what);
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        if (bundle != null) {
          msg.setData(bundle);
        }
        messenger.send(msg);
      } catch (RemoteException e) {
        clients.remove(messenger);
      }
    }
  }

  private void sendMessage(int what) {
    sendMessage(what, NO_ARG);
  }
  private void sendMessage(int what, int arg1) {
    sendMessage(what, arg1, null);
  }

  Runnable postElapsedRunnable = new Runnable() {
    @Override
    public void run() {
      if (mediaPlayer.isPlaying()) {
        int elapsed = (int) mediaPlayer.getCurrentPosition();
        sendMessage(MSG_MEDIA_ELAPSED, elapsed);
        mTimeElapsedHandler.postDelayed(this, 500);
      }
    }
  };

  public static void playOrResumePodcast(Context context, Podcast podcast) {
    Intent intent = new Intent(context, MediaPlayerService.class);
    if (!PrefUtils.getPodcastPlaying(context).equals(podcast.podcastId)) {
      PrefUtils.setPodcastPlaying(context, podcast.podcastId);
      intent.setAction(MediaPlayerService.ACTION_PLAY);
    } else {
      intent.setAction(MediaPlayerService.ACTION_RESUME);
    }
    intent.putExtra(MediaPlayerService.EXTRA_MEDIA, PlaybackUtils.getNextEpisode(context, podcast));
    context.startService(intent);
  }

}
