package fireminder.podcastcatcher.db;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class PlaylistDao {
	private PlaylistSqlHelper dbHelper;
	private SQLiteDatabase db;

	public static String TAG = PlaylistDao.class.getSimpleName();

	public PlaylistDao(Context context) {
		dbHelper = new PlaylistSqlHelper(context);
	}

	public void open() {
		db = dbHelper.getWritableDatabase();
	}

	public void close() {
		db.close();
	}

	public static void cursorToPlaylist(Cursor cursor) {
		Playlist p = Playlist.instance;
		cursor.moveToFirst();
		while (cursor.moveToNext()) {
			Episode e = new Episode();
			e.set_id(cursor.getLong(cursor
					.getColumnIndex(EpisodeSqlHelper.COLUMN_ID)));
			e.setPodcast_id(cursor.getLong(cursor
					.getColumnIndex(EpisodeSqlHelper.COLUMN_PODCAST_ID)));
			e.setDescription(cursor.getString(cursor
					.getColumnIndex(EpisodeSqlHelper.COLUMN_DESCRIP)));
			e.setTitle(cursor.getString(cursor
					.getColumnIndex(EpisodeSqlHelper.COLUMN_TITLE)));
			e.setUrl(cursor.getString(cursor
					.getColumnIndex(EpisodeSqlHelper.COLUMN_URL)));
			e.setPubDate(cursor.getLong(cursor
					.getColumnIndex(EpisodeSqlHelper.COLUMN_PUBDATE)));
			e.setMp3(cursor.getString(cursor
					.getColumnIndex(EpisodeSqlHelper.COLUMN_MP3)));
			p.addEpisode(e);
		}
	}

	public void getPlaylist() {
		Cursor cursor = db.query(PlaylistSqlHelper.TABLE_NAME,
				PlaylistSqlHelper.allColumns, null, null, null, null,
				PlaylistSqlHelper.COLUMN_POSITION);
		cursorToPlaylist(cursor);
	}

	public void insertPlaylist(Playlist p) {
		db.execSQL("DROP TABLE IF EXISTS " + PlaylistSqlHelper.TABLE_NAME);
		db.execSQL(PlaylistSqlHelper.CREATE_STATEMENT);
		for(int i = 0; i < p.episodes.size(); i++){
			Episode e = p.episodes.get(i);
			ContentValues cv = new ContentValues();
			cv.put(EpisodeSqlHelper.COLUMN_PODCAST_ID, e.getPodcast_id());
			cv.put(EpisodeSqlHelper.COLUMN_TITLE, e.getTitle());
			cv.put(EpisodeSqlHelper.COLUMN_DESCRIP, e.getDescription());
			cv.put(EpisodeSqlHelper.COLUMN_URL, e.getUrl());
			cv.put(EpisodeSqlHelper.COLUMN_PUBDATE, e.getPubDate());
			cv.put(EpisodeSqlHelper.COLUMN_MP3, e.getMp3());
			cv.put(PlaylistSqlHelper.COLUMN_POSITION, i);
			db.insert(PlaylistSqlHelper.TABLE_NAME, null, cv);
		}
	}
}
