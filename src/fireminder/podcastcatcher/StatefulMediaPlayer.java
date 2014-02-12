package fireminder.podcastcatcher;

import java.io.File;

import android.media.MediaPlayer;
import android.util.Log;

public class StatefulMediaPlayer extends MediaPlayer {

    public State mState;

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

    public void setDataSource(File file) {
        this.stop();
        mState = State.STOPPED;
            Log.e("TAG", "STOPPED");
        try {
            Log.e("TAG", file.getAbsolutePath());
            super.setDataSource(file.getAbsolutePath());
            super.prepare();
            mState = State.PREPARED;
        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
            mState = State.CREATED;
        }
        
        
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
    
    
}
