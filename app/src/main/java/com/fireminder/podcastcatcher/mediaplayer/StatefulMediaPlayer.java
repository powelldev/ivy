package com.fireminder.podcastcatcher.mediaplayer;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.fireminder.podcastcatcher.models.Episode;

import java.io.IOException;

public class StatefulMediaPlayer implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnCompletionListener {
  private static final String LOG_TAG = StatefulMediaPlayer.class.getSimpleName();

  // TODO double check members in functions

  private Episode media;
  private MediaPlayer mediaPlayer;
  private MediaStateListener mListener;

  private boolean beginPlaybackWhenMediaIsPrepared = false;

  public State mState;

  public StatefulMediaPlayer(MediaStateListener listener) {
    mState = State.CREATED;
    mediaPlayer = new MediaPlayer();
    mediaPlayer.setOnCompletionListener(this);
    mediaPlayer.setOnSeekCompleteListener(this);
    mediaPlayer.setOnInfoListener(this);
    mediaPlayer.setOnErrorListener(this);
    mediaPlayer.setOnPreparedListener(this);
    mediaPlayer.setOnBufferingUpdateListener(this);
    mListener = listener;
  }

  @Override
  public void onBufferingUpdate(MediaPlayer mp, int percent) {
    Log.e(LOG_TAG, "onBufferingUpdate: " + " percent " + percent);
  }

  @Override
  public void onPrepared(MediaPlayer mp) {
    Log.e(LOG_TAG, "onPrepared");
    transitionToState(State.PREPARED);
    if (media.elapsed != 0) {
      mediaPlayer.seekTo((int) media.elapsed);
    }
    if (beginPlaybackWhenMediaIsPrepared) {
      play();
    }
  }

  @Override
  public boolean onError(MediaPlayer mp, int what, int extra) {
    Log.e(LOG_TAG, "onError: " + "what: " + what + " extra: " + extra);
    return false;
  }

  @Override
  public boolean onInfo(MediaPlayer mp, int what, int extra) {
    Log.e(LOG_TAG, "onInfo: " + "what: " + what + " extra: " + extra);
    return false;
  }

  @Override
  public void onSeekComplete(MediaPlayer mp) {
    Log.e(LOG_TAG, "onSeekComplete");
  }

  @Override
  public void onCompletion(MediaPlayer mp) {
    Log.e(LOG_TAG, "onCompletion");
    transitionToState(State.COMPLETED);
  }

  public long getDuration(Context context) {
    long durationLong = 0;
    if (media != null) {
      MediaMetadataRetriever retriever = new MediaMetadataRetriever();
      retriever.setDataSource(context, Uri.parse(media.getUri()));
      String duration =
          retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
      durationLong = Long.parseLong(duration);
      retriever.release();
    }
    return durationLong;
  }

  public long getCurrentPosition() {
    return mediaPlayer.getCurrentPosition();
  }

  public boolean isPlaying() {
    return mediaPlayer.isPlaying();
  }

  public Episode getMedia() {
    return media;
  }

  public enum State {
    STARTED, STOPPED, PAUSED, PREPARED, COMPLETED, CREATED, RELEASED
  }

  /* VERIFY DATA BEFORE PASSED TO MEDIAPLAYER*/
  public void setDataSource(Episode media, boolean playWhenReady) throws IOException {
    if (media == null)
      throw new IOException("media null");
    if (media.getUri() == null)
      throw new IOException("media uri null");

    this.media = media;
    this.beginPlaybackWhenMediaIsPrepared = playWhenReady;
    mediaPlayer.reset();
    mediaPlayer.setDataSource(media.getUri());
    mediaPlayer.prepare();
  }


  private void transitionToState(State state) {
    mState = state;
    if (mListener != null) {
      mListener.onStateUpdated(state);
      mListener.onMediaElapsed(media);
    }
    Log.d(LOG_TAG, "MediaPlayer State: " + state.name());
  }

  public interface MediaStateListener {
    public void onStateUpdated(State state);

    public void onMediaElapsed(Episode media);
  }

  public void playPause() throws IllegalStateException {
    switch (mState) {
      case PAUSED:
      case PREPARED:
        play();
        break;
      case STARTED:
        pause();
        break;
      default:
        throw new IllegalStateException("No data source set.");
    }
  }

  public void start() throws IOException {
    // Load media from playlist if exists
    this.setDataSource(media, false);
  }

  public void play() {
    switch (mState) {
      case PREPARED:
      case PAUSED:
        mediaPlayer.start();
        transitionToState(State.STARTED);
        break;
    }
  }

  public void pause() {
    switch (mState) {
      case STARTED:
        mediaPlayer.pause();
        media.elapsed = mediaPlayer.getCurrentPosition();
        transitionToState(State.PAUSED);
        break;
    }
  }

  boolean wasPlaying = false;

  public void seekTo(int position) {
    switch (mState) {
      case STARTED:
      case PAUSED:
        mediaPlayer.seekTo(position);
        if (wasPlaying) {
          this.play();
        }
        break;
      case PREPARED:
        mediaPlayer.seekTo(position);
        break;
    }
  }

  public void seekStart() {
    switch (mState) {
      case STARTED:
      case PAUSED:
        wasPlaying = mediaPlayer.isPlaying();
        this.pause();
        break;
    }
  }


    /*


    public void stop() {
        switch (mState) {
            case STARTED:
            case PAUSED:
                mediaPlayer.stop();
                mState = State.STOPPED;
                break;
            default:
                break;
        }
    }

    public void pause() {
        switch (mState) {
            case STARTED:
                mediaPlayer.pause();
                mState = State.PAUSED;
                break;
            default:
                break;
        }
    }

    public void startAt(int elapsed) {
        mediaPlayer.seekTo(elapsed);
        this.start();
    }

    public void seek(int time) {

        boolean playing = mediaPlayer.isPlaying();
        if (mState == State.STARTED) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(time);
            if (playing) {
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(listener);
            }
        } else if (mState == State.STOPPED) {

        } else if (mState == State.PAUSED) {

            mediaPlayer.pause();
            mediaPlayer.seekTo(time);
            if (playing) {
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(listener);
            }

        } else if (mState == State.PREPARED) {

            mediaPlayer.seekTo(time);

        } else if (mState == State.CREATED) {

        }
    }

    public void start() {
        switch (mState) {
            case STARTED:
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(listener);
                break;

            case STOPPED:
                this.setDataSource(media);
                break;

            case PAUSED:
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(listener);
                mState = State.STARTED;
                break;

            case PREPARED:
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(listener);
                mState = State.STARTED;
                break;

            case CREATED:
                break;
        }
    }

    public String getCurrentTrack() {

        return mCurrentTrack;
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void setOnCompletionListener(
            MediaPlayer.OnCompletionListener listener) {
        this.listener = listener;
    }

    public void setPlaybackCompleted() {
        mState = State.PREPARED;
    }

*/
}


