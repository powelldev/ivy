package fireminder.podcastcatcher.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import fireminder.podcastcatcher.db.EpisodeDao;

public class SqlHelper extends SQLiteOpenHelper {
    
    public static final String DATABASE_NAME = "podcasts.db";
    public static final int DATABASE_VER = 7;

    public SqlHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + EpisodeDao.TABLE_NAME + " ("
                + EpisodeDao.COLUMN_ID + " integer primary key autoincrement,"
                + EpisodeDao.COLUMN_PODCAST_ID + " integer not null, "
                + EpisodeDao.COLUMN_TITLE + " text not null, "
                + EpisodeDao.COLUMN_DESCRIP + " text, " + EpisodeDao.COLUMN_URL
                + " text, " + EpisodeDao.COLUMN_PUBDATE + " integer, "
                + EpisodeDao.COLUMN_MP3 + " text, "
                + EpisodeDao.COLUMN_DURATION + " text, "
                + EpisodeDao.COLUMN_ELAPSED + " integer);");

        db.execSQL("CREATE TABLE " + PodcastDao.TABLE_NAME + " ("
                + PodcastDao.COLUMN_ID + " integer primary key autoincrement,"
                + PodcastDao.COLUMN_TITLE + " text not null, "
                + PodcastDao.COLUMN_DESCRIP + " text, "
                + PodcastDao.COLUMN_LINK + " text, "
                + PodcastDao.COLUMN_IMAGELINK + " text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + EpisodeDao.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PodcastDao.TABLE_NAME);
        onCreate(db);
    }

}