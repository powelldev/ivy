package fireminder.podcastcatcher.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PodcastSqlHelper extends SQLiteOpenHelper{

	public static final String TABLE_NAME = "podcasts";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_DESCRIP = "description";
	public static final String COLUMN_LINK = "link";
	public static final String COLUMN_IMAGELINK = "imagelink";
	
	public static final String DATABASE_NAME = "podcast.db";
	public static final int DATABASE_VER = 4;
	
	public static final String[] allColumns = 
		{ COLUMN_ID, COLUMN_TITLE, COLUMN_DESCRIP, COLUMN_LINK, COLUMN_IMAGELINK };
	public PodcastSqlHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VER);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(
			"CREATE TABLE " + TABLE_NAME + " (" +
			COLUMN_ID + " integer primary key autoincrement," +
			COLUMN_TITLE + " text not null, " +
			COLUMN_DESCRIP + " text, " +
			COLUMN_LINK + " text, " +
			COLUMN_IMAGELINK + " text);" 
				);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS podcasts");
		onCreate(db);
	}

}
