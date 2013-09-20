package fireminder.podcastcatcher.boot;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import fireminder.podcastcatcher.downloads.ADownloadService;

public class BootService extends IntentService{

	public BootService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(this, ADownloadService.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
		alarmManager.cancel(pi);
		alarmManager.set(AlarmManager.ELAPSED_REALTIME, 10000, pi);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), AlarmManager.INTERVAL_HOUR, pi);
	}

}
