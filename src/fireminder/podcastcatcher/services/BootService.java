package fireminder.podcastcatcher.services;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import fireminder.podcastcatcher.PodcastCatcher;
import fireminder.podcastcatcher.utils.Utils;

public class BootService extends IntentService {

    public BootService() {
        super("BootService");
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e("HAPT", "Starting service");
        PodcastCatcher.getInstance().setContext(getApplicationContext());
        AlarmManager alarmManager = (AlarmManager) this
                .getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, ADownloadService.class);
        
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar
                .getInstance().getTimeInMillis(), Utils.UPDATE_TIMING,
                pi);
    }

}
