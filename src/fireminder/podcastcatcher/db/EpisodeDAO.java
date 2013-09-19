package fireminder.podcastcatcher.db;

import java.util.ArrayList;
import java.util.List;

import fireminder.podcastcatcher.valueobjects.Episode;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class EpisodeDAO {
	private EpisodeSqlHelper dbHelper;
	private SQLiteDatabase db;
	
	public static String TAG = EpisodeDAO.class.getSimpleName();
	
	public EpisodeDAO(Context context){
		dbHelper = new EpisodeSqlHelper(context);
	}
	
	public void open(){
		db = dbHelper.getWritableDatabase();
	}
	
	public void close(){
		db.close();
	}
	
	public static Episode cursorToEpisode(Cursor cursor){
		Episode e = new Episode();
		e.set_id(cursor.getLong(cursor.getColumnIndex(EpisodeSqlHelper.COLUMN_ID)));
		e.setPodcast_id(cursor.getLong(cursor.getColumnIndex(EpisodeSqlHelper.COLUMN_PODCAST_ID)));
		e.setDescription(cursor.getString(cursor.getColumnIndex(EpisodeSqlHelper.COLUMN_DESCRIP)));
		e.setTitle(cursor.getString(cursor.getColumnIndex(EpisodeSqlHelper.COLUMN_TITLE)));
		e.setUrl(cursor.getString(cursor.getColumnIndex(EpisodeSqlHelper.COLUMN_URL)));
		e.setPubDate(cursor.getLong(cursor.getColumnIndex(EpisodeSqlHelper.COLUMN_PUBDATE)));
		e.setMp3(cursor.getString(cursor.getColumnIndex(EpisodeSqlHelper.COLUMN_MP3)));
		return e;
	}
	
	public Episode getEpisode(long id){
		Cursor cursor = db.query(EpisodeSqlHelper.TABLE_NAME, 
				EpisodeSqlHelper.allColumns, 
				EpisodeSqlHelper.COLUMN_ID + " = " + id, 
				null, null, null, null);
		cursor.moveToFirst();
		Episode e =  cursorToEpisode(cursor);
		return e;
	}	
	
	public void updateEpisodeMp3(long eid, String mp3Loc){
		Log.d(TAG, mp3Loc);
		ContentValues cv = new ContentValues();
		cv.put(EpisodeSqlHelper.COLUMN_MP3, mp3Loc);
		db.update(EpisodeSqlHelper.TABLE_NAME, cv, EpisodeSqlHelper.COLUMN_ID + " = " + eid, null);
		
	}
	public Episode insertEpisode(Episode e){
		ContentValues cv = new ContentValues();
		cv.put(EpisodeSqlHelper.COLUMN_PODCAST_ID, e.getPodcast_id());
		cv.put(EpisodeSqlHelper.COLUMN_TITLE, e.getTitle());
		cv.put(EpisodeSqlHelper.COLUMN_DESCRIP, e.getDescription());
		cv.put(EpisodeSqlHelper.COLUMN_URL, e.getUrl());
		cv.put(EpisodeSqlHelper.COLUMN_PUBDATE, e.getPubDate());
		cv.put(EpisodeSqlHelper.COLUMN_MP3, e.getMp3());

		return insertEpisode(cv);
		
	}
	public Episode insertEpisode(ContentValues episodeData){
		long insertId = db.insert(EpisodeSqlHelper.TABLE_NAME, null, episodeData);
		// get recently inserted item
		
		Cursor cursor = db.query(EpisodeSqlHelper.TABLE_NAME, 
				EpisodeSqlHelper.allColumns, 
				EpisodeSqlHelper.COLUMN_ID + " = " + insertId, 
				null, null, null, null);
		
		cursor.moveToFirst();
		Episode e = cursorToEpisode(cursor);
		cursor.close();
		
		return e;
	}

	public Cursor getAllEpisodesAsCursor(long id){	
		return db.query(EpisodeSqlHelper.TABLE_NAME, EpisodeSqlHelper.allColumns, EpisodeSqlHelper.COLUMN_PODCAST_ID + "=" + id, null, null, null, null);
	}
	public void deleteAllEpisodes(long id){
		if(db == null){
			db = dbHelper.getWritableDatabase();
		}
		db.delete(EpisodeSqlHelper.TABLE_NAME, EpisodeSqlHelper.COLUMN_PODCAST_ID + " = ?", new String[] {""+id});
		db.close();
	}
	
	public void deleteEpisode(long id){
		Log.d("query:", ""+db.query(EpisodeSqlHelper.TABLE_NAME, EpisodeSqlHelper.allColumns, EpisodeSqlHelper.COLUMN_ID+ "="+ id, null, null, null, null).getCount());
		Log.d("deleted:", "" + db.delete(EpisodeSqlHelper.TABLE_NAME, EpisodeSqlHelper.COLUMN_ID + " = " + id , null));
		//db.delete(EpisodeSqlHelper.TABLE_NAME, EpisodeSqlHelper.COLUMN_ID + " = ? " , new String[] {""+id});
		}
	public void addMp3(Episode episode){
		
	}
	
	public Episode getLatestEpisode(long id){
		Episode e = null;
		/*
		 * SELECT * 
		 * FROM episodes 
		 * WHERE podcast_id = id 
		 * ORDER BY pubDate DESC
		 */
		Cursor c = db.query(EpisodeSqlHelper.TABLE_NAME, EpisodeSqlHelper.allColumns, 
				EpisodeSqlHelper.COLUMN_PODCAST_ID + " = ?", new String[] {"" + id}, 
				null, null, EpisodeSqlHelper.COLUMN_PUBDATE + " DESC", ""+1);
		if(c.getCount() == 0) {
			return null;
		}
		c.moveToFirst();
		e = cursorToEpisode(c);
		return e;
	}

	public Cursor getAllEpisodesAsCursorByDate(long id) {
		return db.query(EpisodeSqlHelper.TABLE_NAME, EpisodeSqlHelper.allColumns, EpisodeSqlHelper.COLUMN_PODCAST_ID + " = " + id, null, null, null, EpisodeSqlHelper.COLUMN_PUBDATE + " DESC " , null);
	}
	
	public List<Episode> getAllEpisodes(long id) {
		List<Episode> episodes = new ArrayList<Episode>();
		Cursor cursor = db.query(EpisodeSqlHelper.TABLE_NAME, EpisodeSqlHelper.allColumns, EpisodeSqlHelper.COLUMN_PODCAST_ID + " = " + id, null, null, null, EpisodeSqlHelper.COLUMN_PUBDATE + " DESC " , null);
		cursor.moveToFirst();

		do {
			episodes.add(cursorToEpisode(cursor));
		}while(cursor.moveToNext());
		
		return episodes;
	}


}
