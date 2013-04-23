package fireminder.podcastcatcher.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class EpisodeDAO {
	private EpisodeSqlHelper dbHelper;
	private SQLiteDatabase db;
	
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
		return null;
	}
	
	public Episode getEpisode(long id){
		return null;
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
		db.delete(EpisodeSqlHelper.TABLE_NAME, EpisodeSqlHelper.COLUMN_PODCAST_ID + " = ?", new String[] {""+id});
	}
	
	public void deleteEpisode(long id){
		db.delete(EpisodeSqlHelper.TABLE_NAME, EpisodeSqlHelper.COLUMN_ID + " = ? " , new String[] {""+id});
	}
	public void addMp3(Episode episode){
		
	}

}
