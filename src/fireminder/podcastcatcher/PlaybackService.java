package fireminder.podcastcatcher;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;
import fireminder.podcastcatcher.db.Episode;

public class PlaybackService extends Service implements OnPreparedListener{

	MediaPlayer mPlayer = null;
	PlaylistSingle playlist = PlaylistSingle.instance;
	// Updated with state of MediaPlayer
	enum PlayerState {
		PLAYING,
		PAUSED,
		STOPPED,
		PREPARING
	}
	
	PlayerState mState = PlayerState.STOPPED;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_NOT_STICKY;
	}

	@Override
	public void onCreate() {
	}

	void createMediaPlayerIfNeeded() {
		if (mPlayer == null) {
			mPlayer = new MediaPlayer();
			try {
				mPlayer.prepare();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void playCurrentSong(){
		mState = PlayerState.STOPPED;
		Episode e = playlist.getCurrent();
		if(e == null){
			return;
		}
		
		try {
			mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mPlayer.setDataSource(e.getMp3());
			mState = PlayerState.PREPARING;
			mPlayer.prepareAsync();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	void playNextSong(){
		
	}
	void playPauseSong() {
		// Stopped: Start playing next song.
		// Playing: Pause song.
		// Paused:  Start playing current song
		// Preparing: Dunno
		switch(mState){
		
		case STOPPED:
			break;
		
		case PLAYING:
			mPlayer.pause();
			mState = PlayerState.PAUSED;
			break;

		case PAUSED:
			if(!mPlayer.isPlaying()) mPlayer.start();
			mState = PlayerState.PLAYING;
			break;

		case PREPARING:
			break;
		}
	}

	void say(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	public class LocalBinder extends Binder {
		PlaybackService getService() {
			return PlaybackService.this;
		}
	}

	private final IBinder mBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mState = PlayerState.PLAYING;
		if(!mPlayer.isPlaying()){
			mPlayer.start();
		}
	}
}
