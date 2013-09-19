package fireminder.podcastcatcher.db;

import java.util.ArrayList;
import java.util.List;

import fireminder.podcastcatcher.PodcastCatcher;
import fireminder.podcastcatcher.valueobjects.Episode;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class EpisodeDao2 {

	public static final String TABLE_NAME = "episodes";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_PODCAST_ID = "podcast_id";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_DESCRIP = "description";
	public static final String COLUMN_URL = "url";
	public static final String COLUMN_PUBDATE = "pubdate";
	public static final String COLUMN_MP3 = "mp3";

	public static final String DATABASE_NAME = "episode.db";
	public static final int DATABASE_VER = 5;

	/*** A list of all the columns in the episode db, useful for queries */
	public static final String[] allColumns = { COLUMN_ID, COLUMN_PODCAST_ID,
			COLUMN_TITLE, COLUMN_DESCRIP, COLUMN_URL, COLUMN_PUBDATE,
			COLUMN_MP3 };
	public static String TAG = EpisodeDao2.class.getSimpleName();

	public EpisodeDao2() {
	}

	public static Episode cursorToEpisode(Cursor cursor) {
		Episode e = new Episode();
		e.set_id(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
		e.setPodcast_id(cursor.getLong(cursor.getColumnIndex(COLUMN_PODCAST_ID)));
		e.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIP)));
		e.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
		e.setUrl(cursor.getString(cursor.getColumnIndex(COLUMN_URL)));
		e.setPubDate(cursor.getLong(cursor.getColumnIndex(COLUMN_PUBDATE)));
		e.setMp3(cursor.getString(cursor.getColumnIndex(COLUMN_MP3)));
		return e;
	}

	public Episode get(long id) {
		SQLiteDatabase db = new SqlHelper(PodcastCatcher.getInstance()
				.getContext()).getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, allColumns,
				COLUMN_ID + " = " + id, null, null, null, null);
		cursor.moveToFirst();
		Episode e = cursorToEpisode(cursor);
		db.close();
		return e;
	}

	public long update(Episode episode) {
		long id = 0;
		SQLiteDatabase db = new SqlHelper(PodcastCatcher.getInstance()
				.getContext()).getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_PODCAST_ID, episode.getPodcast_id());
		cv.put(COLUMN_TITLE, episode.getTitle());
		cv.put(COLUMN_DESCRIP, episode.getDescription());
		cv.put(COLUMN_URL, episode.getUrl());
		cv.put(COLUMN_PUBDATE, episode.getPubDate());
		cv.put(COLUMN_MP3, episode.getMp3());
		id = db.update(TABLE_NAME, cv, COLUMN_ID + " = " + episode.get_id(), null);
		db.close();
		return id;
	}

	public long insert(Episode e) {
		long id = -1;
		SQLiteDatabase db = new SqlHelper(PodcastCatcher.getInstance()
				.getContext()).getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_PODCAST_ID, e.getPodcast_id());
		cv.put(COLUMN_TITLE, e.getTitle());
		cv.put(COLUMN_DESCRIP, e.getDescription());
		cv.put(COLUMN_URL, e.getUrl());
		cv.put(COLUMN_PUBDATE, e.getPubDate());
		cv.put(COLUMN_MP3, e.getMp3());
		
		id = db.insert(TABLE_NAME, null,
				cv);

		db.close();
		return id;

	}


	public Cursor getAllEpisodesAsCursor(long id) {
		SQLiteDatabase db = new SqlHelper(PodcastCatcher.getInstance()
				.getContext()).getWritableDatabase();
		Cursor cursor =  db.query(TABLE_NAME,
				allColumns, COLUMN_PODCAST_ID
						+ "=" + id, null, null, null, null);
		db.close();
		return cursor;
	}

	public void deleteAllEpisodes(long id) {
		SQLiteDatabase db = new SqlHelper(PodcastCatcher.getInstance()
				.getContext()).getWritableDatabase();
		db.delete(TABLE_NAME,
				COLUMN_PODCAST_ID + " = ?", new String[] { ""
						+ id });
		db.close();
	}

	public void delete(Episode episode){
		SQLiteDatabase db = new SqlHelper(PodcastCatcher.getInstance()
				.getContext()).getWritableDatabase();
		db.delete(TABLE_NAME, COLUMN_ID + " = " + episode.get_id(), null);
		db.close();
	}


	public Episode getLatestEpisode(long id) {
		SQLiteDatabase db = new SqlHelper(PodcastCatcher.getInstance()
				.getContext()).getWritableDatabase();
		Episode e = null;
		/*
		 * SELECT * FROM episodes WHERE podcast_id = id ORDER BY pubDate DESC
		 */
		Cursor c = db.query(TABLE_NAME,
				allColumns, COLUMN_PODCAST_ID
						+ " = ?", new String[] { "" + id }, null, null,
				COLUMN_PUBDATE + " DESC", "" + 1);
		if (c.getCount() == 0) {
			return null;
		}
		c.moveToFirst();
		e = cursorToEpisode(c);
		return e;
	}

	public Cursor getAllEpisodesAsCursorByDate(long id) {
		SQLiteDatabase db = new SqlHelper(PodcastCatcher.getInstance()
				.getContext()).getWritableDatabase();
		Cursor cursor =  db.query(TABLE_NAME,
				allColumns, COLUMN_PODCAST_ID
						+ " = " + id, null, null, null,
				COLUMN_PUBDATE + " DESC ", null);
		return cursor;
	}

	public List<Episode> getAllEpisodes(long id) {
		SQLiteDatabase db = new SqlHelper(PodcastCatcher.getInstance()
				.getContext()).getWritableDatabase();
		List<Episode> episodes = new ArrayList<Episode>();
		Cursor cursor = db.query(TABLE_NAME,
				allColumns, COLUMN_PODCAST_ID
						+ " = " + id, null, null, null,
				COLUMN_PUBDATE + " DESC ", null);
		cursor.moveToFirst();
		do {
			episodes.add(cursorToEpisode(cursor));
		} while (cursor.moveToNext());
		db.close();

		return episodes;
	}

	/***
	 * Class containing commonly referenced strings and variables for
	 * interacting with the database.
	 */
	public static class SqlHelper extends SQLiteOpenHelper {

		public SqlHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VER);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COLUMN_ID
					+ " integer primary key autoincrement," + COLUMN_PODCAST_ID
					+ " integer not null, " + COLUMN_TITLE + " text not null, "
					+ COLUMN_DESCRIP + " text, " + COLUMN_URL + " text, "
					+ COLUMN_PUBDATE + " integer, " + COLUMN_MP3 + " text);");

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS episodes");
			onCreate(db);
		}

	}

}