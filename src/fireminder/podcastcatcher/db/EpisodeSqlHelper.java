package fireminder.podcastcatcher.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/***
 * Class containing commonly referenced strings and variables for 
 * interacting with the database.
 */
public class EpisodeSqlHelper extends SQLiteOpenHelper{

	public static final String TABLE_NAME = "episodes";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_PODCAST_ID = "podcast_id";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_DESCRIP = "description";
	public static final String COLUMN_URL = "url";
	public static final String COLUMN_PUBDATE = "pubdate";
	public static final String COLUMN_MP3 = "mp3";

	public static final String DATABASE_NAME = "episode.db";
	public static final int DATABASE_VER = 1;
	
    /*** A list of all the columns in the episode db, useful for queries */
	public static final String[] allColumns = 
		{ COLUMN_ID, COLUMN_PODCAST_ID, COLUMN_TITLE, COLUMN_DESCRIP, COLUMN_URL, COLUMN_PUBDATE, COLUMN_MP3 };
	
	public EpisodeSqlHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VER);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(
			"CREATE TABLE " + TABLE_NAME + " (" +
			COLUMN_ID + " integer primary key autoincrement," +
			COLUMN_PODCAST_ID + " integer not null, " +
			COLUMN_TITLE + " text not null, " +
			COLUMN_DESCRIP + " text, " +
			COLUMN_URL + " text, " +
			COLUMN_PUBDATE + " integer, " +
			COLUMN_MP3 + " text);" 
				);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS podcasts");
		onCreate(db);
	}

}
