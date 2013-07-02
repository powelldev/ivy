package fireminder.podcastcatcher.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PlaylistSqlHelper extends SQLiteOpenHelper{

    public static final String TABLE_NAME = "playlist";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_POSITION = "position";
    public static final String COLUMN_EPISODE_ID = "episode_id";

    public static final String DATABASE_NAME = "playlist.db";
    public static final int DATABASE_VER = 1;

    public static final String[] allColumns = 
        { COLUMN_ID, COLUMN_POSITION, COLUMN_EPISODE_ID };
    
    public PlaylistSqlHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " integer primary key autoincrement," +
                    COLUMN_POSITION + " integer not null, " +
                    COLUMN_EPISODE_ID +" integer not null );" 
                    );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
