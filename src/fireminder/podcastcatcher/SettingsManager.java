package fireminder.podcastcatcher;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import fireminder.podcastcatcher.services.ADownloadService;
import fireminder.podcastcatcher.utils.Utils;

public class SettingsManager {
    public static void onSettingsChangeListener(Context context,
            SharedPreferences preference, String key) {
        if (key.matches(context.getResources().getString(
                R.string.prefSyncFrequency))) {
            int hoursBetweenUpdates;
            try {
                hoursBetweenUpdates = Integer.parseInt(preference.getString(
                        context.getResources().getString(
                                R.string.prefSyncFrequency), "24"));
            } catch (Exception e) {
                hoursBetweenUpdates = 591;
                SharedPreferences.Editor editor = preference.edit();
                editor.putString(
                        context.getResources().getString(
                                R.string.prefSyncFrequency), "591");
                editor.commit();
            }
            AlarmManager am = (AlarmManager) context
                    .getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, ADownloadService.class);
            PendingIntent pi = PendingIntent.getService(context, 0, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            am.setRepeating(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(),
                    hoursBetweenUpdates * 60 * 60 * 1000, pi);
            Log.e(Utils.TAG, "Hours between updates: " + hoursBetweenUpdates);
        } else if (key.matches(context.getResources().getString(
                R.string.prefAutoDelete))) {
            Log.e(Utils.TAG,
                    "Auto Delete: " + preference.getBoolean(key, false));
        } else if (key.matches(context.getResources().getString(
                R.string.prefDeleteXShows))) {
            int deleteThisMany;
            try {
                deleteThisMany = Integer.parseInt(preference.getString(context
                        .getResources().getString(R.string.prefDeleteXShows),
                        "2147483647"));
            } catch (Exception e) {
                deleteThisMany = Integer.MAX_VALUE;
                SharedPreferences.Editor editor = preference.edit();
                editor.putString(
                        context.getResources().getString(
                                R.string.prefSyncFrequency),
                        String.valueOf(deleteThisMany));
                editor.commit();
            }
            Log.e(Utils.TAG, "Delete x many: " + deleteThisMany);
        }

        Log.e(Utils.TAG, preference.getAll().toString());
    }
}
