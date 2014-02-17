package fireminder.podcastcatcher.utils;

import java.io.File;
import java.io.FileWriter;

import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

public class Utils {

    public static final long UPDATE_TIMING = 60000;
    private static final String LOGFILE = Environment
            .getExternalStorageDirectory() + File.separator + "creek.txt";

    public static String getStringFromCursor(Cursor cursor, String index) {
        return cursor.getString(cursor.getColumnIndex(index));
    }

    public static void log(String info) {
        try {
            File log = new File(LOGFILE);
            FileWriter out = new FileWriter(log, true);
            java.util.Date time = new java.util.Date(System.currentTimeMillis());
            out.write("\n" + time.getDate() + ":" + time.getHours() +":" + time.getMinutes() + ":"+ time.getSeconds() + ": ");
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
