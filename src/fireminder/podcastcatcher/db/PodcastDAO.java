package fireminder.podcastcatcher.db;

import java.util.ArrayList;
import java.util.List;

import fireminder.podcastcatcher.valueobjects.Podcast;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public class PodcastDAO {
	
	private PodcastSqlHelper dbHelper;
	private SQLiteDatabase db;
	private Context context;
	
	public PodcastDAO(Context context){
		dbHelper = new PodcastSqlHelper(context);
		this.context = context;
	}
	
	public void open(){
		db = dbHelper.getWritableDatabase();
	}
	
	public void close(){
		db.close();
	}
	public static Podcast cursorToPodcast(Cursor cursor){
		cursor.moveToFirst();
		Podcast podcast = new Podcast();
		String title = "";
		String link = "";
		String descrip = "";
		String imagePath = null;
		int id;
	
		id = (int) cursor.getLong(cursor.getColumnIndex(PodcastSqlHelper.COLUMN_ID));
		title = cursor.getString(cursor.getColumnIndex(PodcastSqlHelper.COLUMN_TITLE));
		descrip = cursor.getString(cursor.getColumnIndex(PodcastSqlHelper.COLUMN_DESCRIP));
		link = cursor.getString(cursor.getColumnIndex(PodcastSqlHelper.COLUMN_LINK));
		try{
		imagePath = cursor.getString(cursor.getColumnIndex(PodcastSqlHelper.COLUMN_IMAGELINK));
		} catch(Exception e){ e.printStackTrace();}
		podcast.setDescription(descrip); podcast.setTitle(title); podcast.setId(id); podcast.setLink(link); podcast.setImagePath(imagePath);
		
		return podcast;
	}
	
	
	public Podcast getPodcast(long id){
		Cursor cursor = db.query(PodcastSqlHelper.TABLE_NAME, 
			PodcastSqlHelper.allColumns, 
			PodcastSqlHelper.COLUMN_ID + " = " + id, 
			null, null, null, null);
		Podcast podcast = cursorToPodcast(cursor);
		return podcast;
	}
	public Cursor getAllPodcastsAsCursor(){	
		return db.query(PodcastSqlHelper.TABLE_NAME, PodcastSqlHelper.allColumns, null, null, null, null, PodcastSqlHelper.COLUMN_TITLE);
	}
	public List<Podcast> getAllPodcasts(){
		ArrayList<Podcast> podcasts = new ArrayList<Podcast>();
		
		Cursor cursor = db.query(PodcastSqlHelper.TABLE_NAME, PodcastSqlHelper.allColumns, null, null, null, null, null);
		cursor.moveToFirst();
		do {
			Podcast p = cursorToPodcast(cursor);
			podcasts.add(p);
		} while(cursor.moveToNext());
		
		return podcasts;
	}
	
/*	public Podcast createAndInsertPodcast(List<String> data){
		ContentValues cv = new ContentValues();
		cv.put(PodcastSQLiteOpenHelper.COLUMN_TITLE, data.get(0));
		cv.put(PodcastSQLiteOpenHelper.COLUMN_DESCRIP, data.get(1));
		cv.put(PodcastSQLiteOpenHelper.COLUMN_LINK, data.get(2));
		cv.put(PodcastSQLiteOpenHelper.COLUMN_IMAGELINK, data.get(3));
		
		long insertId = db.insert(PodcastSQLiteOpenHelper.TABLE_NAME,
				null, cv);
		
		// get recently inserted item
		
		Cursor cursor = db.query(PodcastSQLiteOpenHelper.TABLE_NAME, 
				PodcastSQLiteOpenHelper.allColumns, 
				PodcastSQLiteOpenHelper.COLUMN_ID + " = " + insertId, 
				null, null, null, null);
		
		cursor.moveToFirst();
		Podcast p = cursorToPodcast(cursor);
		cursor.close();
		
		return p;
	}
	*/
	public Podcast insertPodcast(ContentValues podcastData){
		long insertId = db.insert(PodcastSqlHelper.TABLE_NAME, null, podcastData);// get recently inserted item
		
		Cursor cursor = db.query(PodcastSqlHelper.TABLE_NAME, 
				PodcastSqlHelper.allColumns, 
				PodcastSqlHelper.COLUMN_ID + " = " + insertId, 
				null, null, null, null);
		
		cursor.moveToFirst();
		Podcast p = cursorToPodcast(cursor);
		cursor.close();
		
		return p;
		
	}
	public Podcast createAndInsertPodcast(String name){
		ContentValues cv = new ContentValues();
		cv.put(PodcastSqlHelper.COLUMN_TITLE, name);
		
		long insertId = db.insert(PodcastSqlHelper.TABLE_NAME,
				null, cv);
		
		// get recently inserted item
		
		Cursor cursor = db.query(PodcastSqlHelper.TABLE_NAME, 
				PodcastSqlHelper.allColumns, 
				PodcastSqlHelper.COLUMN_ID + " = " + insertId, 
				null, null, null, null);
		
		cursor.moveToFirst();
		Podcast p = cursorToPodcast(cursor);
		cursor.close();
		
		return p;
	}
	
	public void deletePodcast(long id){
		EpisodeDAO edao = new EpisodeDAO(context);
		edao.open();
		edao.deleteAllEpisodes(id);
		db.delete(PodcastSqlHelper.TABLE_NAME, 
				PodcastSqlHelper.COLUMN_ID + " = ? " , 
				new String[] {""+id});
	}
	
	public void updatePodcastImagelink(Podcast podcast) {
		ContentValues args = new ContentValues();
		args.put(PodcastSqlHelper.COLUMN_IMAGELINK, podcast.getImagePath());
		long debug = db.update(PodcastSqlHelper.TABLE_NAME, args, PodcastSqlHelper.COLUMN_ID + " = " + podcast.getId() , null);
		Log.d("rows updated: ", "" + debug);
		
		
	}

}
