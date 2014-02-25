package fireminder.podcastcatcher;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.File;

import fireminder.podcastcatcher.valueobjects.Episode;

public class StatefulMediaPlayer extends MediaPlayer {

    public State mState;
    private long mEpisodeId;

    private static final String TAG = StatefulMediaPlayer.class.getSimpleName();
    private String mCurrentTrack = "";
    public enum State {
        STARTED, STOPPED, PAUSED, PREPARED, CREATED;
    }

    public StatefulMediaPlayer() {
        super();
        mState = State.CREATED;
    }

    public State getState() {
        return mState;
    }

    public void setDataSource(Episode episode) {
        this.reset();
        try {
            mEpisodeId = episode.get_id();
            File file = new File(episode.getMp3());
            Log.e(TAG, file.getAbsolutePath());
            super.setDataSource(file.getAbsolutePath());
            super.prepare();
            mState = State.PREPARED;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            mState = State.CREATED;
        }
        
        
    }
    
    public void reset() {
        super.reset();
        mState = State.CREATED;
    }
    public void stop() {
        if (mState == State.STARTED) {

            super.stop();

            mState = State.STOPPED;

        } else if (mState == State.STOPPED) {

        } else if (mState == State.PAUSED) {
            
            super.stop();
            
            mState = State.STOPPED;

        } else if (mState == State.PREPARED) {

        } else if (mState == State.CREATED) {

        }
    }
    public void pause() {
        if (mState == State.STARTED) {

            super.pause();

            mState = State.PAUSED;

        } else if (mState == State.STOPPED) {

        } else if (mState == State.PAUSED) {

        } else if (mState == State.PREPARED) {

        } else if (mState == State.CREATED) {

        }
    }
    
    public void startAt(int elapsed) {
        super.seekTo(elapsed);
        this.start();
    }

    public void seek(int time) {
        if (mState == State.STARTED) {
            super.seekTo(time);
        } else if (mState == State.STOPPED) {

        } else if (mState == State.PAUSED) {

            super.seekTo(time);

        } else if (mState == State.PREPARED) {

            super.seekTo(time);

        } else if (mState == State.CREATED) {

        }
    }

    public void start() {
        if (mState == State.STARTED) {

        } else if (mState == State.STOPPED) {

        } else if (mState == State.PAUSED) {

            super.start();

            mState = State.STARTED;

        } else if (mState == State.PREPARED) {

            super.start();

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
    
    
}
