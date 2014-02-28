package fireminder.podcastcatcher.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import fireminder.podcastcatcher.utils.Utils;
import fireminder.podcastcatcher.valueobjects.Podcast;

public class PodcastDao {

    public static final String TABLE_NAME = "podcasts";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIP = "description";
    public static final String COLUMN_LINK = "link";
    public static final String COLUMN_IMAGELINK = "imagelink";

    public static final String[] allColumns = { COLUMN_ID, COLUMN_TITLE,
            COLUMN_DESCRIP, COLUMN_LINK, COLUMN_IMAGELINK };

    Context context;
    public PodcastDao(Context context) {
        this.context = context;
    }

    public ArrayList<Podcast> getAll() {
        ArrayList<Podcast> podcasts = new ArrayList<Podcast>();
        SQLiteDatabase db = SqlHelper.getInstance(context)
                .getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, allColumns, null, null, null,
                null, null);
        cursor.moveToFirst();
        do {
            podcasts.add(createPodcastFromCursor(cursor));
        } while (cursor.moveToNext());
        return podcasts;
    }

    public Podcast get(long id) {
        SQLiteDatabase db = SqlHelper.getInstance(context)
                .getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, allColumns,
                COLUMN_ID + " = " + id, null, null, null, null);
        cursor.moveToFirst();
        return createPodcastFromCursor(cursor);
    }

    public static Podcast createPodcastFromCursor(Cursor cursor) {
        Podcast podcast = new Podcast();
        podcast.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
        podcast.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
        podcast.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIP)));
        podcast.setLink(cursor.getString(cursor.getColumnIndex(COLUMN_LINK)));
        podcast.setImagePath(Utils.getStringFromCursor(cursor, COLUMN_IMAGELINK));
        return podcast;
    }

    public void delete(Podcast podcast) {
        SQLiteDatabase db = SqlHelper.getInstance(context).getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = " + podcast.getId(), null);
    }

    public void update(Podcast podcast) {
        SQLiteDatabase db = SqlHelper.getInstance(context).getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ID, podcast.getId());
        cv.put(COLUMN_TITLE, podcast.getTitle());
        cv.put(COLUMN_LINK, podcast.getLink());
        cv.put(COLUMN_DESCRIP, podcast.getDescription());
        cv.put(COLUMN_IMAGELINK, podcast.getImagePath());
        db.update(TABLE_NAME, cv, COLUMN_ID + " = " + podcast.getId(), null);
    }

    public long insert(Podcast podcast) {
        long id = -1;
        SQLiteDatabase db = SqlHelper.getInstance(context).getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TITLE, podcast.getTitle());
        cv.put(COLUMN_LINK, podcast.getLink());
        cv.put(COLUMN_DESCRIP, podcast.getDescription());
        cv.put(COLUMN_IMAGELINK, podcast.getImagePath());
        id = db.insert(TABLE_NAME, null, cv);
        return id;
    }

    //TODO will change when ContentProvider added
    public Cursor getAllPodcastsAsCursor(){
        SQLiteDatabase db = SqlHelper.getInstance(context).getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, allColumns,
                null, null, null, null, COLUMN_TITLE + " ASC");
        return cursor;
    }

    public void delete(long mId) {
       Podcast podcast = get(mId); 
       new EpisodeDao(context).deleteDataOnAll(podcast);
       delete(podcast);
    }
    
}