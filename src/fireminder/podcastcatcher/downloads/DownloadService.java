package fireminder.podcastcatcher.downloads;

import java.util.Calendar;

import fireminder.podcastcatcher.activities.NotificationActivity;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class DownloadService extends IntentService{

	public DownloadService() {
		super("DownloadService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(this, NotificationActivity.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
		alarmManager.cancel(pi);
//		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), 5000, pi);
	}

}
