package fireminder.podcastcatcher;

import java.io.IOException;
import java.lang.ref.WeakReference;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import fireminder.podcastcatcher.db.Playlist;

public class PlaybackService extends Service implements OnCompletionListener,
		OnClickListener, OnSeekBarChangeListener {

	static MediaPlayer mPlayer;
	Playlist playlist;

	WeakReference<Button> btnPlay;
	WeakReference<SeekBar> mSeekBar;

	enum State {
		PLAYING, PAUSED, PREPARED, STOPPED;
	};

	State state;
	private Handler mHandler = new Handler();
	private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			if (mPlayer != null) {
				int progress = mPlayer.getCurrentPosition() / 1000;
				mSeekBar.get().setProgress(progress);
			}
			mHandler.postDelayed(mRunnable, 1000);
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		this.state = State.STOPPED;
		playlist = Playlist.instance;

		super.onCreate();
	}

	public int onStartCommand(Intent intent, int flags, int startId) {

		initUI();

		mPlayer = new MediaPlayer();
		mPlayer.setOnCompletionListener(this);
		mPlayer.reset();
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mPlayer.setWakeMode(getApplicationContext(),
				PowerManager.PARTIAL_WAKE_LOCK);

		String uri = intent.getStringExtra("EpisodeUri");
		if (uri != null) {
			playSong(uri);
		}

		return START_STICKY;
	}

	public void initUI() {
		btnPlay = new WeakReference<Button>(PlayerFragment.play_btn);
		mSeekBar = new WeakReference<SeekBar>(PlayerFragment.seekBar);

		btnPlay.get().setOnClickListener(this);
		mSeekBar.get().setOnSeekBarChangeListener(this);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.play_btn:
			if (mPlayer.isPlaying())
				mPlayer.pause();
			else {
				mPlayer.start();
			}
			Toast.makeText(getApplicationContext(), "Play button",
					Toast.LENGTH_LONG).show();
		}
	}

	public void playSong(String uri) {
		try {
			mPlayer.reset();
			mPlayer.setDataSource(uri);
			mPlayer.prepare();
			mPlayer.start();
			updateSeekBar();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void updateSeekBar() {
		mSeekBar.get().setProgress(0);
		mSeekBar.get().setMax(mPlayer.getDuration());
		mHandler.postDelayed(mRunnable, 1000);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub

	}
}
