package com.fireminder.podcastcatcher.mediaplayer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.models.Episode;
import com.fireminder.podcastcatcher.models.Podcast;
import com.fireminder.podcastcatcher.utils.PlaybackUtils;
import com.fireminder.podcastcatcher.utils.PrefUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaPlayerService extends Service implements StatefulMediaPlayer.MediaStateListener {

  public static final String ACTION_PLAY = "action_play";
  public static final String ACTION_PLAY_PAUSE = "action_play_pause";
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

  // Flags for communication from the MediaPlayer to our clients
  public static final int MSG_MEDIA_COMPLETE = 201;
  public static final int MSG_MEDIA_DURATION = 600;
  public static final int MSG_MEDIA_ELAPSED = 601;
  public static final int MSG_MEDIA_TITLE = 602;
  /* Flag to send duration, elapsed, album art, and episode data to the view to be updated */
  public static final int MSG_HANDSHAKE_WITH_VIEW = 700;
  private static final String LOG_TAG = MediaPlayerService.class.getSimpleName();

  private final StatefulMediaPlayer mediaPlayer = new StatefulMediaPlayer(this);

  private final Handler myHandler = new Handler(new Handler.Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      switch (msg.what) {
        case MSG_ADD_CLIENT:
          clients.add(msg.replyTo);
          sendInfoMessage("Bound to client");
          break;
        case MSG_SET_DATA:
          final Episode media = msg.getData().getParcelable(EXTRA_MEDIA);
          setData(media);
          break;
        case MSG_START:
          start();
          break;
        case MSG_PLAY_PAUSE:
          playPause();
          break;
        case MSG_SEEK_START:
          seekStart();
          break;
        case MSG_SEEK_END:
          seekEnd(msg.arg1);
          break;
        case MSG_BACK_THIRTY:
          seekEnd((int) mediaPlayer.getCurrentPosition() - (30 * 1000));
          break;
        case MSG_FORWARD_THIRTY:
          seekEnd((int) mediaPlayer.getCurrentPosition() + (30 * 1000));
          break;
        case MSG_HANDSHAKE_WITH_VIEW:
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

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null && intent.getAction() != null) {
      if (intent.getAction().equals(ACTION_PLAY)) {
        Episode media = intent.getParcelableExtra(EXTRA_MEDIA);
        setData(media);
      } else if (intent.getAction().equals(ACTION_PLAY_PAUSE)) {
        Episode media = intent.getParcelableExtra(EXTRA_MEDIA);
        setData(media);
      }
    }
    return START_STICKY;
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


  private void setData(Episode media) {
    try {
      mediaPlayer.setDataSource(media, true);
      this.startForeground(1, new NotificationCompat.Builder(getApplicationContext())
              .setSmallIcon(R.mipmap.ic_launcher)
              .setContentText(media.description)
              .setContentTitle(media.title)
              .setPriority(Integer.MAX_VALUE)
              .setWhen(0)
              .addAction(R.drawable.ic_play_arrow_white_48dp, "test", null)
              .addAction(R.drawable.ic_skip_next_white_48dp, "test", null)
              .build()
      );
      Podcast podcast = PlaybackUtils.getPodcastOf(getApplicationContext(), media);
      int prefetch = PrefUtils.getNumEpisodesToPrefetch(getApplicationContext());
      PlaybackUtils.downloadNextXEpisodes(getApplicationContext(), podcast, prefetch);
    } catch (IOException e) {
      Toast.makeText(getApplicationContext(), "Out of episodes", Toast.LENGTH_SHORT).show();
      e.printStackTrace();
    }
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

  @Override
  public void onStateUpdated(StatefulMediaPlayer.State state) {
    sendInfoMessage("State Change: " + state.name());
    int duration;
    Bundle bundle;
    switch (state) {
      case STARTED:
        duration = (int) mediaPlayer.getDuration(getApplicationContext());
        sendMessage(MSG_MEDIA_DURATION, duration);
        mTimeElapsedHandler.removeCallbacks(postElapsedRunnable);
        mTimeElapsedHandler.post(postElapsedRunnable);

        duration = (int) mediaPlayer.getDuration(getApplicationContext());
        sendMessage(MSG_MEDIA_DURATION, duration);
        mTimeElapsedHandler.removeCallbacks(postElapsedRunnable);
        mTimeElapsedHandler.post(postElapsedRunnable);
        bundle = new Bundle();
        bundle.putParcelable(EXTRA_MEDIA, mediaPlayer.getMedia());
        bundle.putString(EXTRA_MEDIA_CONTENT, PlaybackUtils.getEpisodeImage(getApplicationContext(), mediaPlayer.getMedia()));
        sendMessage(MSG_HANDSHAKE_WITH_VIEW, (int) mediaPlayer.getCurrentPosition(), (int) mediaPlayer.getDuration(getApplicationContext()), bundle);

        break;
      case PREPARED:
        // TODO this is written twice
        if (mediaPlayer.isPlaying()) {
          duration = (int) mediaPlayer.getDuration(getApplicationContext());
          sendMessage(MSG_MEDIA_DURATION, duration);
          mTimeElapsedHandler.removeCallbacks(postElapsedRunnable);
          mTimeElapsedHandler.post(postElapsedRunnable);
          bundle = new Bundle();
          bundle.putParcelable(EXTRA_MEDIA, mediaPlayer.getMedia());
          bundle.putString(EXTRA_MEDIA_CONTENT, PlaybackUtils.getEpisodeImage(getApplicationContext(), mediaPlayer.getMedia()));
          sendMessage(MSG_HANDSHAKE_WITH_VIEW, (int) mediaPlayer.getCurrentPosition(), (int) mediaPlayer.getDuration(getApplicationContext()), bundle);
        }
        break;
      case PAUSED:
        PlaybackUtils.updateEpisodeElapsed(getApplicationContext(),
            mediaPlayer.getMedia(), mediaPlayer.getCurrentPosition());
        break;
      case COMPLETED:
        onMediaCompleted();
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
    PlaybackUtils.setEpisodeComplete(getApplicationContext(), completed);

    // Play next or stop playing
    Podcast podcast = PlaybackUtils.getPodcastOf(getApplicationContext(), completed);
    Episode next = PlaybackUtils.getNextEpisode(getApplicationContext(), podcast);

    if (next == null) {
      stopForeground(true);
    } else {
      setData(next);
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
      intent.setAction(MediaPlayerService.ACTION_PLAY_PAUSE);
    }
    intent.putExtra(MediaPlayerService.EXTRA_MEDIA, PlaybackUtils.getNextEpisode(context, podcast));
    context.startService(intent);
  }
}
