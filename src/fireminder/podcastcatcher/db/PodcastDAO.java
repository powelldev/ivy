package fireminder.podcastcatcher.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public class PodcastDAO {

private PodcastSQLiteOpenHelper dbHelper;
private SQLiteDatabase db;

public PodcastDAO(Context context){
	dbHelper = new PodcastSQLiteOpenHelper(context);
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
	byte[] imagelink = null;
	int id;

	id = (int) cursor.getLong(cursor.getColumnIndex(PodcastSQLiteOpenHelper.COLUMN_ID));
	title = cursor.getString(cursor.getColumnIndex(PodcastSQLiteOpenHelper.COLUMN_TITLE));
	descrip = cursor.getString(cursor.getColumnIndex(PodcastSQLiteOpenHelper.COLUMN_DESCRIP));
	link = cursor.getString(cursor.getColumnIndex(PodcastSQLiteOpenHelper.COLUMN_LINK));
	try{
	imagelink = cursor.getBlob(cursor.getColumnIndex(PodcastSQLiteOpenHelper.COLUMN_IMAGELINK));
	} catch(Exception e){ e.printStackTrace();}
	podcast.setDescription(descrip); podcast.setTitle(title); podcast.setId(id); podcast.setLink(link); podcast.setImagelink(imagelink);
	
	return podcast;
}


public Podcast getPodcast(long id){
	Cursor cursor = db.query(PodcastSQLiteOpenHelper.TABLE_NAME, 
		PodcastSQLiteOpenHelper.allColumns, 
		PodcastSQLiteOpenHelper.COLUMN_ID + " = " + id, 
		null, null, null, null);
	Podcast podcast = cursorToPodcast(cursor);
	return podcast;
}
public Cursor getAllPodcastsAsCursor(){	
	return db.query(PodcastSQLiteOpenHelper.TABLE_NAME, PodcastSQLiteOpenHelper.allColumns, null, null, null, null, null);
}
public List<Podcast> getAllPodcasts(){
	ArrayList<Podcast> podcasts = new ArrayList<Podcast>();
	
	Cursor cursor = db.query(PodcastSQLiteOpenHelper.TABLE_NAME, PodcastSQLiteOpenHelper.allColumns, null, null, null, null, null);
	cursor.moveToFirst();
	while(cursor.moveToNext()){
		Podcast p = cursorToPodcast(cursor);
		podcasts.add(p);
	}
	
	return podcasts;
}

public Podcast createAndInsertPodcast(List<String> data){
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

public Podcast insertPodcast(ContentValues podcastData){
	long insertId = db.insert(PodcastSQLiteOpenHelper.TABLE_NAME, null, podcastData);// get recently inserted item
	
	Cursor cursor = db.query(PodcastSQLiteOpenHelper.TABLE_NAME, 
			PodcastSQLiteOpenHelper.allColumns, 
			PodcastSQLiteOpenHelper.COLUMN_ID + " = " + insertId, 
			null, null, null, null);
	
	cursor.moveToFirst();
	Podcast p = cursorToPodcast(cursor);
	cursor.close();
	
	return p;
	
}
public Podcast createAndInsertPodcast(String name){
	ContentValues cv = new ContentValues();
	cv.put(PodcastSQLiteOpenHelper.COLUMN_TITLE, name);
	
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

public void deletePodcast(long id){
	db.delete(PodcastSQLiteOpenHelper.TABLE_NAME, PodcastSQLiteOpenHelper.COLUMN_ID + " = ? " , new String[] {""+id});
}

public void updatePodcast(Podcast podcast) {
	ContentValues args = new ContentValues();
	args.put(PodcastSQLiteOpenHelper.COLUMN_IMAGELINK, podcast.getImagelink());
	long debug = db.update(PodcastSQLiteOpenHelper.TABLE_NAME, args, PodcastSQLiteOpenHelper.COLUMN_ID + " = " + podcast.get_id() , null);
	Log.d("rows updated: ", "" + debug);
	
	
}

}
