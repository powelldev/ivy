package com.fireminder.podcastcatcher.mediaplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;
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

public class MediaPlayerService extends Service implements StatefulMediaPlayer.MediaStateListener, AudioManager.OnAudioFocusChangeListener {

  private static final String LOG_TAG = MediaPlayerService.class.getSimpleName();

  private static final int SKIP_TIME_MILLIS = 30 * 1000;

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


  // Flag for messages that do not contain an argument
  private static final int NO_ARG = -1;
  public static final int MSG_IS_PLAYING = 702;

  final int NOTIFICATION_ID = 1;

  private final StatefulMediaPlayer mediaPlayer = new StatefulMediaPlayer(this);


  /**
   * If we are playing this podcast, resume. Otherwise start playing it.
   */
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

  /* COMMUNICATION TOOLS WITH REMOTES */

  /*
   * We utilize two methods of communication: actions passed along with intents and
   * binding the service. This allows us to use remote controls and update the playback
   * view without excessive broadcasts.
   */

  private final Handler myHandler = new Handler(new Handler.Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      switch (msg.what) {
        case MSG_ADD_CLIENT:
          clients.add(msg.replyTo);
          break;
        case MSG_SET_DATA:
          final Episode media = msg.getData().getParcelable(EXTRA_MEDIA);
          processSetDataRequest(media, true);
          break;
        case MSG_START:
          start();
          break;
        case MSG_PLAY_PAUSE:
          // if we are returning from having closed the application, load
          // the last playing episode and begin playback, otherwise toggle play/pause
          String episodeId = PrefUtils.getEpisodePlaying(getApplicationContext());
          if (!TextUtils.isEmpty(episodeId) && !mediaPlayer.isPlaying()) {
            Cursor cursor = getApplicationContext().getContentResolver().query(PodcastCatcherContract.Episodes.buildEpisodeUri(episodeId), null, null, null, null);
            cursor.moveToFirst();
            processSetDataRequest(Episode.parseEpisodeFromCursor(cursor), true);
          } else {
            playPause();
          }
          break;
        case MSG_SEEK_START:
          processSeek();
          break;
        case MSG_SEEK_END:
          processSeekEnding(msg.arg1);
          break;
        case MSG_BACK_THIRTY:
          processSeekEnding((int) mediaPlayer.getCurrentPosition() - (SKIP_TIME_MILLIS));
          break;
        case MSG_FORWARD_THIRTY:
          processSeekEnding((int) mediaPlayer.getCurrentPosition() + (SKIP_TIME_MILLIS));
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

  private final List<Messenger> clients = new ArrayList<>();
  private final Messenger mMessenger = new Messenger(myHandler);
  private final Handler mTimeElapsedHandler = new Handler();

  // Alert view which episode we are playing, this may be replaced in future by using SharedPrefs to store playing episode
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

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null && intent.getAction() != null) {
      switch (intent.getAction()) {
        case ACTION_PLAY:
        case ACTION_RESUME:
          Episode media = intent.getParcelableExtra(EXTRA_MEDIA);
          processSetDataRequest(media, true);
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

  private void sendMessage(int what) {
    sendMessage(what, NO_ARG);
  }
  private void sendMessage(int what, int arg1) {
    sendMessage(what, arg1, null);
  }

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

  /**
   * Wrapper for sending messages to all clients.
   *
   * @param what   argument for Message object
   * @param arg1   argument for Message object
   * @param arg2   argument for Message object
   * @param bundle optional argument for data to pass to messenger
   */
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

  @Override
  public IBinder onBind(Intent intent) {
    return mMessenger.getBinder();
  }

  /* Media Playback control requests */

  /*
   * Here we wrap actions for our media player.
   * Their responses will be handled in our MediaPlayer Listener
   */

  // Start media playback. This assumes that the media data has already been set.
  private void start() {
    try {
      mediaPlayer.start();
    } catch (IOException e) {
      sendErrorMessage(e.getMessage());
      e.printStackTrace();
    }
  }

  /* Stop media playback with the assumption that setData will need to be called again
    prior to resuming playback  */
  private void stop() {
    try {
      mediaPlayer.stop();
    } catch (IllegalStateException e) {
      sendErrorMessage(e.getMessage());
      e.printStackTrace();
    }
  }

  // Toggle play/pause
  private void playPause() {
    try {
      mediaPlayer.playPause();
    } catch (IllegalStateException e) {
      sendErrorMessage(e.getMessage());
      e.printStackTrace();
    }
  }

  private void next() {
    onMediaCompleted();
  }

  private void previous() {
    Episode current = mediaPlayer.getMedia();

    // Play next or stop playing
    Podcast podcast = PlaybackUtils.getPodcastOf(getApplicationContext(), current);
    Episode previous = PlaybackUtils.getPreviousEpisode(getApplicationContext(), podcast, current);

    if (previous != null) {
      PlaybackUtils.setEpisodeComplete(getApplicationContext(), previous, false);
      processSetDataRequest(previous, true);
    }
  }

  private void processSeek() {
    mediaPlayer.seekStart();
  }

  private void processSeekEnding(int position) {
    mediaPlayer.seekTo(position);
  }


  private void processSetDataRequest(Episode media, boolean beginPlaybackImmediately) {
    try {
      mediaPlayer.setDataSource(media, beginPlaybackImmediately);
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

    RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
        .setSmallIcon(R.drawable.ic_white_icon)
        .setContent(remoteViews)
        .setContentIntent(pIntent);
        /*
        .setContentText(episode.description)
        .setContentTitle(episode.title)
        .setPriority(Integer.MAX_VALUE)
        .setWhen(0)
        .setContentIntent(pIntent)
        .addAction(R.drawable.ic_play_arrow_white_48dp, getString(R.string.play), pauseTogglePendingIntent)
        .addAction(R.drawable.ic_skip_next_white_48dp, getString(R.string.next), skipPendingIntent);
        */

    remoteViews.setImageViewResource(R.id.button_play_pause, mediaPlayer.isPlaying() ? R.drawable.ic_pause_black_48dp : R.drawable.ic_play_arrow_black_48dp);
    remoteViews.setOnClickPendingIntent(R.id.button_next, skipPendingIntent);
    remoteViews.setOnClickPendingIntent(R.id.button_play_pause, pauseTogglePendingIntent);
    remoteViews.setImageViewResource(R.id.image, R.drawable.ic_white_icon);
    remoteViews.setTextViewText(R.id.title, episode.title);
    remoteViews.setTextViewText(R.id.description, episode.description);
    return builder.build();
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
    Notification notification;
    switch (state) {
      case STARTED:
          renamethis(true);
        PrefUtils.setEpisodePlaying(getApplicationContext(), mediaPlayer.getMedia().episodeId);
        PrefUtils.setPodcastPlaying(getApplicationContext(), mediaPlayer.getMedia().podcastId);
        notification = setupNotification(mediaPlayer.getMedia());
        startForeground(1, notification);
        sendMessage(MSG_IS_PLAYING, mediaPlayer.isPlaying() ? 1 : 0);
        break;
      case PREPARED:
        AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        int result = am.requestAudioFocus(this,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN);

        if (mediaPlayer.isPlaying()) {
            renamethis(true);
        }
        break;
      case PAUSED:
        PlaybackUtils.updateEpisodeElapsed(getApplicationContext(),
            mediaPlayer.getMedia(), mediaPlayer.getCurrentPosition());
        sendMessage(MSG_IS_PLAYING, mediaPlayer.isPlaying() ? 1 : 0);
        notification = setupNotification(mediaPlayer.getMedia());
        startForeground(1, notification);
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
      processSetDataRequest(next, true);
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

  /* Runnable to update the view with elapsed time. */
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

  boolean wasPlayingBeforeAudioLoss = false;
  @Override
  public void onAudioFocusChange(int focusChange) {
    switch (focusChange) {
      case AudioManager.AUDIOFOCUS_GAIN:
        if (wasPlayingBeforeAudioLoss) {
          mediaPlayer.play(); // Should only be called after LOSS_TRANSIENT
        }
        break;
      case AudioManager.AUDIOFOCUS_LOSS:
        // Will not be getting focus back, stop playback.
        if (mediaPlayer.isPlaying()) {
          PrefUtils.setEpisodePlaying(getApplicationContext(), mediaPlayer.getMedia().episodeId);
        }
        stop();
        stopForeground(true);
        break;
      case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: // Podcast Pet Peeve: Never lower volume.
                                                   // Stop, then resume if necessary
      case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
        wasPlayingBeforeAudioLoss = mediaPlayer.isPlaying();
        mediaPlayer.pause();
        break;
    }
  }

  private boolean DEBUG = false;
  private void sendInfoMessage(String message) {
    if (DEBUG) {
      Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
  }

  private void sendErrorMessage(String message) {
    if (DEBUG) {
      Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
  }

}
