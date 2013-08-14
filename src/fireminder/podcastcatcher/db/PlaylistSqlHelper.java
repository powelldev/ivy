package fireminder.podcastcatcher.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** A Playlist will simply be a list of episodes - therefore the table will resemble the Episode one */
public class PlaylistSqlHelper extends SQLiteOpenHelper{

	public static final String TABLE_NAME = "playlist";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_PODCAST_ID = "podcast_id";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_DESCRIP = "description";
	public static final String COLUMN_URL = "url";
	public static final String COLUMN_PUBDATE = "pubdate";
	public static final String COLUMN_MP3 = "mp3";
	public static final String COLUMN_POSITION = "position";

	public static final String DATABASE_NAME = "playlist.db";
	public static final int DATABASE_VER = 1;
	
	public static final String[] allColumns = 
		{ COLUMN_ID, COLUMN_PODCAST_ID, COLUMN_TITLE, COLUMN_DESCRIP, COLUMN_URL, COLUMN_PUBDATE, COLUMN_MP3, COLUMN_POSITION};
	
	public PlaylistSqlHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VER);
	}
	
	public static final String CREATE_STATEMENT = 
			"CREATE TABLE " + TABLE_NAME + " (" +
			COLUMN_ID + " integer primary key autoincrement," +
			COLUMN_PODCAST_ID + " integer not null, " +
			COLUMN_TITLE + " text not null, " +
			COLUMN_DESCRIP + " text, " +
			COLUMN_URL + " text, " +
			COLUMN_PUBDATE + " integer, " +
			COLUMN_MP3 + " text, " +  
			COLUMN_POSITION + " integer);";

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
			COLUMN_MP3 + " text, " +  
			COLUMN_POSITION + " integer);"
				);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS playlist");
		onCreate(db);
	}


}
