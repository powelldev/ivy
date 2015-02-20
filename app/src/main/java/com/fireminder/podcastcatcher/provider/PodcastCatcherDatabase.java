package com.fireminder.podcastcatcher.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.fireminder.podcastcatcher.provider.PodcastCatcherContract.EpisodeColumns;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract.PlaylistItemColumns;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract.PodcastColumns;

public class PodcastCatcherDatabase extends SQLiteOpenHelper {

    private static final String LOG_TAG = PodcastCatcherDatabase.class.getSimpleName();
    private static final String DATABASE_NAME = "podcastcatcher.db";

    private static final int VER_2015_DEBUG_A = 10;
    private static final int CUR_DATABASE_VERSION = VER_2015_DEBUG_A;

    private final Context mContext;

    interface Tables {
        String PODCASTS = "podcasts";
        String EPISODES = "episodes";
        String PLAYLIST_ITEMS = "playlist_items";
    }

    public PodcastCatcherDatabase(Context context) {
        super(context, DATABASE_NAME, null, CUR_DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.PODCASTS + " ("
                        + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + PodcastColumns.PODCAST_ID + " TEXT UNIQUE NOT NULL, "
                        + PodcastColumns.PODCAST_TITLE + " TEXT NOT NULL, "
                        + PodcastColumns.PODCAST_DESCRIPTION + " TEXT NOT NULL, "
                        + PodcastColumns.PODCAST_FEED + " TEXT NOT NULL, "
                        + PodcastColumns.PODCAST_IMAGEURL + " TEXT NOT NULL "
                        + ") "
        );

        db.execSQL("CREATE TABLE " + Tables.EPISODES + " ("
                        + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + EpisodeColumns.EPISODE_ID + " TEXT UNIQUE NOT NULL, "
                        + PodcastColumns.PODCAST_ID + " TEXT NOT NULL, "
                        + EpisodeColumns.EPISODE_TITLE + " TEXT NOT NULL, "
                        + EpisodeColumns.EPISODE_DESCRIPTION + " TEXT NOT NULL, "
                        + EpisodeColumns.EPISODE_STREAM_URI + " TEXT NOT NULL, "
                        + EpisodeColumns.EPISODE_LOCAL_URI + " TEXT , "
                        + EpisodeColumns.EPISODE_PUBLICATION_DATE + " TEXT NOT NULL, "
                        + EpisodeColumns.EPISODE_CONTENT_DURATION + " INTEGER , "
                        + EpisodeColumns.EPISODE_PERCENT_ELAPSED + " REAL "
                        + ") "
        );

        db.execSQL("CREATE TABLE " + Tables.PLAYLIST_ITEMS + " ("
                        + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + EpisodeColumns.EPISODE_ID + " TEXT UNIQUE NOT NULL, "
                        + PlaylistItemColumns.PLAYLIST_ITEM_ORDER + " INTEGER NOT NULL "
                        + ") "
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Tables.PODCASTS);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.EPISODES);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.PLAYLIST_ITEMS);
        onCreate(db);
    }

}
