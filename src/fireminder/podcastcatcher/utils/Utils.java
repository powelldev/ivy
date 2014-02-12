package fireminder.podcastcatcher.utils;

import android.database.Cursor;

public class Utils {

    public static String getStringFromCursor(Cursor cursor, String index){
        return cursor.getString(cursor.getColumnIndex(index));
    }
}
