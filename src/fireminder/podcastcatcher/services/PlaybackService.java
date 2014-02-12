package fireminder.podcastcatcher.services;

import java.io.File;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.StatefulMediaPlayer;
import fireminder.podcastcatcher.activities.MainActivity;

public class PlaybackService extends Service {

	private StatefulMediaPlayer mPlayer;

	@Override
	public void onCreate() {
		mPlayer = new StatefulMediaPlayer();
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			if (intent.getAction().contains("START")) {
				this.setForeground(true);
				File file = new File(Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_PODCASTS, "01_NOCTURNAL_Episode_1.mp3");
				Log.e("HAPT", file.getAbsolutePath());
				if (file.exists()) {
					Log.e("HAPT", file.getAbsolutePath());
					mPlayer.setDataSource(file);
					mPlayer.start();
					mHandler.post(updateProgressRunnable);
				}
			}
			else if (intent.getAction().contains("REWIND")) {
				this.setForeground(false);
				mPlayer.pause();
				mHandler.removeCallbacks(updateProgressRunnable);
			}
			Log.e("HAPT", intent.getDataString());
		} catch (Exception e) {
			Log.e("HAPT", "Err" + e.getLocalizedMessage());
		}
		return Service.START_STICKY;
	}

	private void setForeground(boolean on) {
		if (on) {
		Notification notification = new Notification(R.drawable.ic_launcher,
				getText(R.string.app_name), System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(this, "Title", "Text", pendingIntent);
		startForeground(42, notification);
		} else {
			stopForeground(true);
		}
	}
	

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	Handler mHandler = new Handler();
	Runnable updateProgressRunnable = new Runnable() {

		@Override
		public void run() {
			Log.e("TAG", mPlayer.getCurrentTrack());
			Log.e("TAG", "" + mPlayer.getCurrentPosition());
			mHandler.postDelayed(this, 1000);
		}
		
	};
}
