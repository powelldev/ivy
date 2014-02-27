package fireminder.podcastcatcher.db;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import fireminder.podcastcatcher.PodcastCatcher;
import fireminder.podcastcatcher.utils.Utils;
import fireminder.podcastcatcher.valueobjects.Episode;
import fireminder.podcastcatcher.valueobjects.Podcast;

public class EpisodeDao {

    public static final String TABLE_NAME = "episodes";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PODCAST_ID = "podcast_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIP = "description";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_PUBDATE = "pubdate";
    public static final String COLUMN_MP3 = "mp3";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_ELAPSED = "elapsed";
    public static final String COLUMN_PLAYLIST = "playlist";

    /*** A list of all the columns in the episode db, useful for queries */
    public static final String[] allColumns = { COLUMN_ID, COLUMN_PODCAST_ID,
            COLUMN_TITLE, COLUMN_DESCRIP, COLUMN_URL, COLUMN_PUBDATE,
            COLUMN_MP3, COLUMN_DURATION, COLUMN_ELAPSED, COLUMN_PLAYLIST };
    public static String TAG = EpisodeDao.class.getSimpleName();

    Context context;

    public EpisodeDao(Context context) {
        this.context = context;
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
        e.setDuration(Utils.getStringFromCursor(cursor, COLUMN_DURATION));
        e.setElapsed(Utils.getIntFromCursor(cursor, COLUMN_ELAPSED));
        e.setElapsed(Utils.getIntFromCursor(cursor, COLUMN_PLAYLIST));
        return e;
    }

    public Episode get(long id) {
        SQLiteDatabase db = new SqlHelper(context).getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, allColumns,
                COLUMN_ID + " = " + id, null, null, null, null);
        cursor.moveToFirst();
        Episode e = cursorToEpisode(cursor);
        db.close();
        return e;
    }

    public long update(Episode episode) {
        long id = 0;
        SQLiteDatabase db = new SqlHelper(context).getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PODCAST_ID, episode.getPodcast_id());
        cv.put(COLUMN_TITLE, episode.getTitle());
        cv.put(COLUMN_DESCRIP, episode.getDescription());
        cv.put(COLUMN_URL, episode.getUrl());
        cv.put(COLUMN_PUBDATE, episode.getPubDate());
        cv.put(COLUMN_MP3, episode.getMp3());
        cv.put(COLUMN_DURATION, episode.getDuration());
        cv.put(COLUMN_ELAPSED, episode.getElapsed());
        cv.put(COLUMN_PLAYLIST, episode.getPlaylistRank());
        id = db.update(TABLE_NAME, cv, COLUMN_ID + " = " + episode.get_id(),
                null);
        db.close();
        return id;
    }

    public long insert(Episode e) {
        long id = -1;
        SQLiteDatabase db = new SqlHelper(this.context).getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PODCAST_ID, e.getPodcast_id());
        cv.put(COLUMN_TITLE, e.getTitle());
        cv.put(COLUMN_DESCRIP, e.getDescription());
        cv.put(COLUMN_URL, e.getUrl());
        cv.put(COLUMN_PUBDATE, e.getPubDate());
        cv.put(COLUMN_MP3, e.getMp3());
        cv.put(COLUMN_DURATION, e.getDuration());
        cv.put(COLUMN_ELAPSED, e.getElapsed());
        cv.put(COLUMN_PLAYLIST, e.getPlaylistRank());

        id = db.insert(TABLE_NAME, null, cv);

        db.close();
        return id;

    }

    public Cursor getAllEpisodesAsCursor(long id) {
        SQLiteDatabase db = new SqlHelper(context).getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, allColumns, COLUMN_PODCAST_ID
                + "=" + id, null, null, null, null);
        return cursor;
    }

    public void deleteAllEpisodes(long id) {
        SQLiteDatabase db = new SqlHelper(context).getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_PODCAST_ID + " = ?", new String[] { ""
                + id });
        db.close();
    }

    public void delete(Episode episode) {
        SQLiteDatabase db = new SqlHelper(context).getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = " + episode.get_id(), null);
        db.close();
    }

    public Episode getLatestEpisode(long id) {
        SQLiteDatabase db = new SqlHelper(context).getWritableDatabase();
        Episode e = null;
        /*
         * SELECT * FROM episodes WHERE podcast_id = id ORDER BY pubDate DESC
         */
        Cursor c = db.query(TABLE_NAME, allColumns, COLUMN_PODCAST_ID + " = ?",
                new String[] { "" + id }, null, null, COLUMN_PUBDATE + " DESC",
                "" + 1);
        if (c.getCount() == 0) {
            return null;
        }
        c.moveToFirst();
        e = cursorToEpisode(c);
        db.close();
        return e;
    }

    public Cursor getAllEpisodesAsCursorByDate(long id) {
        SQLiteDatabase db = new SqlHelper(context).getWritableDatabase();
        Cursor cursor = db
                .query(TABLE_NAME, allColumns, COLUMN_PODCAST_ID + " = " + id,
                        null, null, null, COLUMN_PUBDATE + " DESC ", null);
        return cursor;
    }

    public List<Episode> getAllEpisodes(long id) {
        SQLiteDatabase db = new SqlHelper(context).getWritableDatabase();
        List<Episode> episodes = new ArrayList<Episode>();
        Cursor cursor = db
                .query(TABLE_NAME, allColumns, COLUMN_PODCAST_ID + " = " + id,
                        null, null, null, COLUMN_PUBDATE + " DESC ", null);
        cursor.moveToFirst();
        do {
            episodes.add(cursorToEpisode(cursor));
        } while (cursor.moveToNext());
        db.close();

        return episodes;
    }

    public Cursor getAllRecentEpisodes() {
        SQLiteDatabase db = new SqlHelper(context).getWritableDatabase();
        long lastWeekInMillis = Calendar.getInstance().getTimeInMillis()
                - (7 * 24 * 60 * 60 * 1000);
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
                + COLUMN_PUBDATE + " >= " + lastWeekInMillis + " ORDER BY "
                + COLUMN_PUBDATE + " DESC ", null);

        return cursor;
    }

    public Cursor getPlaylistEpisodesAsCursor() {
        SQLiteDatabase db = new SqlHelper(context
                ).getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
                + COLUMN_PLAYLIST + " >= 1" + " ORDER BY " + COLUMN_PLAYLIST
                + " ASC ", null);

        return cursor;
    }

    public int getNumberOfEpisodesInPlaylist() {
        SQLiteDatabase db = new SqlHelper(context
                ).getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
                + COLUMN_PLAYLIST + " >= 1" + " ORDER BY " + COLUMN_PLAYLIST
                + " ASC ", null);

        return cursor.getCount();
    }

    public void clearDataOn(long mId) {
        Episode episode = get(mId);
        File file = new File(episode.getMp3());
        if (file.exists()) {
            file.delete();
        }
        episode.setElapsed(0);
    }

    public void clearDataOnAll(long episodeId) {
        List<Episode> episodes = getAllEpisodes(get(episodeId).getPodcast_id());
        for (Episode e : episodes) {
            clearDataOn(e.get_id());
        }
    }

    public void deleteDataOnAll(Podcast podcast) {
        List<Episode> episodes = getAllEpisodes(podcast.getId());
        for (Episode e : episodes) {
            clearDataOn(e.get_id());
            this.delete(e);
        }
    }

    public List<Episode> getPlaylistEpisodes() {
        Cursor c = getPlaylistEpisodesAsCursor();
        List<Episode> episodes = new ArrayList<Episode>();
        if (c.moveToFirst()) {
            do {
                episodes.add(cursorToEpisode(c));
            } while (c.moveToNext());
        }
        return episodes;
    }

}