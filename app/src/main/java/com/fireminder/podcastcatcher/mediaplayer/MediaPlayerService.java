package com.fireminder.podcastcatcher.mediaplayer;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.models.Episode;
import com.fireminder.podcastcatcher.models.Playlist;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;
import com.fireminder.podcastcatcher.utils.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaPlayerService extends Service implements StatefulMediaPlayer.MediaStateListener {

  public static final String ACTION_PLAY = "action_play";
  public static final String EXTRA_MEDIA = "extra_item_to_play";
  public static final String EXTRA_MEDIA_CONTENT = "extra_media_content";

  // Message for adding the a caller's messenger to our list of clients.
  public static final int MSG_ADD_CLIENT = 100;

  public static final int MSG_START = 1000;

  // Flags for communication between client view controls and
  // the MediaPlayer
  public static final int MSG_SET_DATA = 200;
  public static final int MSG_SET_QUEUE = 201;
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
  public static final int MSG_PING_UPDATE_VIEW = 700;
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
        case MSG_PING_UPDATE_VIEW:
          if (mediaPlayer.isPlaying()) {
            int duration = (int) mediaPlayer.getDuration(getApplicationContext());
            sendMessage(MSG_MEDIA_DURATION, duration);
            mTimeElapsedHandler.removeCallbacks(postElapsedRunnable);
            mTimeElapsedHandler.post(postElapsedRunnable);
          }
          sendMessage(MSG_PING_UPDATE_VIEW, (int) mediaPlayer.getCurrentPosition(), (int) mediaPlayer.getDuration(getApplicationContext()) , null);
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
    } catch (IOException e) {
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
    switch (state) {
      case STARTED:
        int duration = (int) mediaPlayer.getDuration(getApplicationContext());
        sendMessage(MSG_MEDIA_DURATION, duration);
        mTimeElapsedHandler.removeCallbacks(postElapsedRunnable);
        mTimeElapsedHandler.post(postElapsedRunnable);
        break;
      case COMPLETED:
        onMediaCompleted();
        break;
    }
  }
private void onMediaCompleted() {
  // Update View
  Bundle bundle = new Bundle();
  bundle.putParcelable(MediaPlayerService.EXTRA_MEDIA, mediaPlayer.getMedia());
  sendMessage(MSG_MEDIA_COMPLETE, -1, bundle);


  Uri playlistEpisodeUri = PodcastCatcherContract.Playlist.buildEpisodesDirUri();

  Cursor cursor = getApplicationContext().getContentResolver().query(PodcastCatcherContract.Playlist.buildEpisodesDirUri(),
      null, null, null, PodcastCatcherContract.Playlist.PLAYLIST_ORDER + " ASC");

  Playlist playlist = Playlist.parsePlaylistFromCursor(cursor);
  for (int i = 0; i < playlist.episodes.size(); i++) {
    if (playlist.episodes.get(i).episode_id.equals(mediaPlayer.getMedia().episode_id)) {
      if (++i >= playlist.episodes.size()) {
        this.stopForeground(true);
      } else {
        this.setData(playlist.episodes.get(i));
        getApplicationContext().getContentResolver().delete(PodcastCatcherContract.Playlist.CONTENT_URI, PodcastCatcherContract.Episodes.EPISODE_ID+ "=?", new String[] {playlist.episodes.get(--i).episode_id});
      }

      break;
    }
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
}
