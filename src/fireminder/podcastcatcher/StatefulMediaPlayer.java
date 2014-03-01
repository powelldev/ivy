package fireminder.podcastcatcher;

import java.io.File;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;
import fireminder.podcastcatcher.utils.Utils;
import fireminder.podcastcatcher.valueobjects.Episode;

public class StatefulMediaPlayer {

    private MediaPlayer mediaPlayer;
    public State mState;
    private Episode episode;

    private OnCompletionListener listener;

    private static final String TAG = StatefulMediaPlayer.class.getSimpleName();
    private String mCurrentTrack = "";

    public enum State {
        STARTED, STOPPED, PAUSED, PREPARED, CREATED, RELEASED;
    }

    public StatefulMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mState = State.CREATED;
    }

    public State getState() {
        return mState;
    }

    public void release() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mState = State.RELEASED;
    }

    public void setDataSource(Episode episode) {
        this.reset();
        try {
            this.episode = episode;
            episode.get_id();
            File file = new File(episode.getMp3());
            Log.e(Utils.TAG, TAG + " File data source" + file.getAbsolutePath());
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.prepare();
            mState = State.PREPARED;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            mState = State.CREATED;
        }

    }

    public void reset() {
        mediaPlayer.reset();
        mState = State.CREATED;
    }

    public void stop() {
        if (mState == State.STARTED) {

            mediaPlayer.stop();

            mState = State.STOPPED;

        } else if (mState == State.STOPPED) {

        } else if (mState == State.PAUSED) {

            mediaPlayer.stop();

            mState = State.STOPPED;

        } else if (mState == State.PREPARED) {

        } else if (mState == State.CREATED) {

        }
    }

    public void pause() {
        if (mState == State.STARTED) {

            mediaPlayer.pause();

            mState = State.PAUSED;

        } else if (mState == State.STOPPED) {

        } else if (mState == State.PAUSED) {

        } else if (mState == State.PREPARED) {

        } else if (mState == State.CREATED) {

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
        if (mState == State.STARTED) {

            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(listener);
        } else if (mState == State.STOPPED) {
            this.setDataSource(episode);

        } else if (mState == State.PAUSED) {

            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(listener);

            mState = State.STARTED;

        } else if (mState == State.PREPARED) {

            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(listener);

            mState = State.STARTED;

        } else if (mState == State.CREATED) {

        }
    }

    public String getCurrentTrack() {

        return mCurrentTrack;
    }

    public long getPlayingEpisodeId() {
        return episode.get_id();
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

}
