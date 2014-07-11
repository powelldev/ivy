package fireminder.podcastcatcher.utils;

import java.io.File;
import java.io.FileWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.util.Log;
import fireminder.podcastcatcher.services.ADownloadService;

public class Utils {

    public static final ColorDrawable action_bar_blue = new ColorDrawable(Color.argb(255, 86, 116, 185));
    private static final String LOGFILE = Environment.getExternalStorageDirectory() + File.separator + "creek.txt";
    public static final String TAG = "PodcastCatcher";
    public static final String DEFUALT_UPDATE = "24";

    public static void setDownloadSchedulerToRunEvery(Context context, int milliseconds) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, ADownloadService.class);

        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

        Calendar cal = Calendar.getInstance();
        long triggerAtMillis;
        long intervalMillis = Long.MAX_VALUE;
        switch (milliseconds) {
        case 30 * 60 * 1000:
            cal.set(Calendar.MINUTE, 30);
            cal.set(Calendar.SECOND, 0);
            intervalMillis = 30 * 60 * 1000;
            break;
        case 60 * 60 * 1000:
            cal.set(Calendar.HOUR_OF_DAY, 1);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            intervalMillis = 60 * 60 * 1000;
            break;
        case 3 * 60 * 60 * 1000:
            cal.set(Calendar.HOUR_OF_DAY, 3);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            intervalMillis = 3 * 60 * 60 * 1000;
            break;
        case 8 * 60 * 60 * 1000:
            cal.set(Calendar.HOUR_OF_DAY, 8);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            intervalMillis = 8 * 60 * 60 * 1000;
            break;
        case 24 * 60 * 60 * 1000:
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            intervalMillis = 24 * 60 * 60 * 1000;
            break;
        default:
            break;
        }
        
        triggerAtMillis = cal.getTimeInMillis();
        
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, intervalMillis, pi);
    }

    public static String getPodcastDir() {
        return Environment.getExternalStorageState() + File.separator + Environment.DIRECTORY_PODCASTS;
    }

    public static String getStringFromCursor(Cursor cursor, String index) {
        return cursor.getString(cursor.getColumnIndex(index));
    }

    /***
     * Checks if internet connection is available by querying google.com
     */
    public static boolean isHTTPAvailable() {
        try {
            URL url = new URL("http://www.google.com");
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setConnectTimeout(1000);
            urlConn.getContent();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static void log(String info) {
        try {
            File log = new File(LOGFILE);
            FileWriter out = new FileWriter(log, true);
            java.util.Date time = new java.util.Date(System.currentTimeMillis());
            out.write("\n" + time.toString());
            out.write(info);
            out.close();
        } catch (Exception e) {
            Log.e("Utils", "Unable to write to log: " + e.getMessage());
        }
    }

    public static void log(String string, String string2) {
        log(string + " " + string2);
    }

    public static int getIntFromCursor(Cursor cursor, String index) {
        return cursor.getInt(cursor.getColumnIndex(index));
    }

}
