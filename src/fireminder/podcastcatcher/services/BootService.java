package fireminder.podcastcatcher.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;
import fireminder.podcastcatcher.PodcastCatcher;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.utils.Utils;

public class BootService extends IntentService {

    public BootService() {
        super("BootService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e("HAPT", "Starting service");
        AlarmManager alarmManager = (AlarmManager) this
                .getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, ADownloadService.class);

        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);

        int hoursBetweenUpdates = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(
                getResources().getString(R.string.prefSyncFrequency), Utils.DEFUALT_UPDATE));

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                hoursBetweenUpdates * 60 * 60 * 1000, pi);

        this.stopSelf();
    }

}
