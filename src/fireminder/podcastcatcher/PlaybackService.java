package fireminder.podcastcatcher;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class PlaybackService extends Service implements
		MediaPlayer.OnPreparedListener {
	public static final String ACTION_PLAY = "fireminder.podcastcatcher.PlaybackService.PLAY";
	public static final String ACTION_PAUSE = "fireminder.podcastcatcher.PlaybackService.PAUSE";
	public static final String ACTION_STOP = "fireminder.podcastcatcher.PlaybackService.STOP";
	MediaPlayer player = null;

	private final IBinder mBinder = new MyBinder();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent.getAction();
		Log.e("PlaybackService", action);
		if (action.equals(ACTION_PLAY)) {
			String songPath = intent.getStringExtra("songPath");
			if(songPath == null){
				return 0;
			}/*
			player.setOnPreparedListener(this);
			Uri uri = Uri.parse(songPath);
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			try {
				player.setDataSource(this, uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
			player.prepareAsync();
			*/
		} else if (action.equals(ACTION_STOP)) {
			//player.stop();
		} else if (action.equals(ACTION_PAUSE)){
//			player.pause();
		}
		return 0;
	}
	
	@Override
	public void onCreate() {
		player = new MediaPlayer();
		Log.e("PlaybackService", "Created");
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mp.start();
	}

	public MediaPlayer getPlayer() {
		return player;
	}

	public int getMediaPlayerDuration() {
		return player.getDuration();
	}

	public int getMediaPlayerCurrentPos() {
		return player.getCurrentPosition();
	}

	public void stop(){
		player.stop();
	}
	public void pause(){
		player.pause();
	}
	public void resume(){
		player.start();
	}
	public class MyBinder extends Binder {
		PlaybackService getService() {
			return PlaybackService.this;
		}
	}
}
