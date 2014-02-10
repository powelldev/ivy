package fireminder.podcastcatcher.boot;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import fireminder.podcastcatcher.downloads.ADownloadService;

public class BootService extends IntentService {

	public BootService() {
		super("BootService");
	}
	
	public static int MILLIS_UPDATE = 1 * 60000;

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.e("HAPT", "Starting service");
		AlarmManager alarmManager = (AlarmManager) this
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent("fireminder.podcastcatcher.downloads.ADownloadService");
		
		PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
		//PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar
				.getInstance().getTimeInMillis() + MILLIS_UPDATE, MILLIS_UPDATE * 60000,
				pi);
	}

}
