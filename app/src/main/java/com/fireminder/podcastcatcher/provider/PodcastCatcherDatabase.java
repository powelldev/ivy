package com.fireminder.podcastcatcher.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.fireminder.podcastcatcher.provider.PodcastCatcherContract.*;

public class PodcastCatcherDatabase extends SQLiteOpenHelper {

  private static final String LOG_TAG = PodcastCatcherDatabase.class.getSimpleName();
  private static final String DATABASE_NAME = "podcastcatcher.db";

  private static final int VER_2015_DEBUG_A = 39;
  private static final int CUR_DATABASE_VERSION = VER_2015_DEBUG_A;

  public PodcastCatcherDatabase(Context context) {
    super(context, DATABASE_NAME, null, CUR_DATABASE_VERSION);
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

    db.execSQL(
        "CREATE TRIGGER DELETE_EPISODES_ASSOCIATED_WITH_PODCASTS AFTER DELETE ON " +
            Tables.PODCASTS + " BEGIN DELETE FROM " + Tables.EPISODES +
            " WHERE " + PodcastCatcherContract.Podcasts.PODCAST_ID + " =old." + PodcastCatcherContract.Podcasts.PODCAST_ID +
            "; END;");

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
            + EpisodeColumns.EPISODE_PERCENT_ELAPSED + " REAL, "
            + EpisodeColumns.EPISODE_IS_COMPLETE + " INTEGER, "
            + EpisodeColumns.EPISODE_IS_DOWNLOADED + " INTEGER "
            + ") "
    );

  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS " + Tables.PODCASTS);
    db.execSQL("DROP TABLE IF EXISTS " + Tables.EPISODES);
    onCreate(db);
  }

  public interface Tables {
    String PODCASTS = "podcasts";
    String EPISODES = "episodes";
  }

}
