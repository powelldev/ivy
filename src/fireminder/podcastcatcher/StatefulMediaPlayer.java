package fireminder.podcastcatcher;

import java.io.File;

import android.media.MediaPlayer;
import android.util.Log;
import fireminder.podcastcatcher.utils.Utils;
import fireminder.podcastcatcher.valueobjects.Episode;

public class StatefulMediaPlayer {

    private MediaPlayer mediaPlayer;
    public State mState;
    private long mEpisodeId;
    private Episode episode;

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
            mEpisodeId = episode.get_id();
            File file = new File(episode.getMp3());
            Log.e(TAG, file.getAbsolutePath());
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
            if (playing)
                mediaPlayer.start();
        } else if (mState == State.STOPPED) {

        } else if (mState == State.PAUSED) {

            mediaPlayer.pause();
            mediaPlayer.seekTo(time);
            if (playing)
                mediaPlayer.start();

        } else if (mState == State.PREPARED) {

            mediaPlayer.pause();
            mediaPlayer.seekTo(time);
            if (playing)
                mediaPlayer.start();

        } else if (mState == State.CREATED) {

        }
    }

    public void start() {
        if (mState == State.STARTED) {

            mediaPlayer.start();
        } else if (mState == State.STOPPED) {
            this.setDataSource(episode);
            
        } else if (mState == State.PAUSED) {

            mediaPlayer.start();

            mState = State.STARTED;

        } else if (mState == State.PREPARED) {

            mediaPlayer.start();

            mState = State.STARTED;

        } else if (mState == State.CREATED) {

        }
    }

    public String getCurrentTrack() {

        return mCurrentTrack;
    }

    public long getPlayingEpisodeId() {
        return mEpisodeId;
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        mediaPlayer.setOnCompletionListener(listener);
    }

}
