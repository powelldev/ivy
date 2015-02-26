package com.fireminder.podcastcatcher.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.fireminder.podcastcatcher.provider.PodcastCatcherContract.Episodes;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract.Playlist;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract.Podcasts;
import com.fireminder.podcastcatcher.provider.PodcastCatcherDatabase.Tables;
import com.fireminder.podcastcatcher.utils.Logger;

public class PodcastCatcherProvider extends ContentProvider {

  private static final String LOG_TAG = PodcastCatcherProvider.class.getSimpleName();
  private static final UriMatcher sUrlMatcher = buildUriMatcher();

  private static final int PODCASTS = 100;
  private static final int PODCASTS_ID = 101;
  private static final int EPISODES = 200;
  private static final int EPISODES_ID = 201;
  private static final int EPISODES_ON_PODCAST = 202;
  private static final int PLAYLIST = 300;
  private static final int PLAYLIST_ID = 301;
  private static final int PLAYLIST_ID_EPISODES = 302;

  private PodcastCatcherDatabase mOpenHelper;

  private static UriMatcher buildUriMatcher() {
    final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    final String authority = PodcastCatcherContract.CONTENT_AUTHORITY;

    matcher.addURI(authority, "podcasts", PODCASTS);
    matcher.addURI(authority, "podcasts/*", PODCASTS_ID);

    matcher.addURI(authority, "episodes", EPISODES);
    matcher.addURI(authority, "episodes/*", EPISODES_ID);

    matcher.addURI(authority, "playlist", PLAYLIST);
    matcher.addURI(authority, "playlist/episodes", PLAYLIST_ID_EPISODES);
    matcher.addURI(authority, "playlist/*", PLAYLIST_ID);

    return matcher;
  }

  @Override
  public boolean onCreate() {
    mOpenHelper = new PodcastCatcherDatabase(getContext());
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
    final int match = sUrlMatcher.match(uri);
    final SelectionBuilder builder = buildExpandedSelection(uri, match);

    Cursor cursor = builder
        .where(selection, selectionArgs)
        .query(db, projection, sortOrder);

    if (getContext() != null) {
      cursor.setNotificationUri(getContext().getContentResolver(), uri);
    }

    return cursor;


  }

  @Override
  public String getType(Uri uri) {
    final int match = sUrlMatcher.match(uri);
    switch (match) {
      case PODCASTS:
        return Podcasts.CONTENT_TYPE;
      case PODCASTS_ID:
        return Podcasts.CONTENT_ITEM_TYPE;
      case EPISODES:
        return Episodes.CONTENT_TYPE;
      case EPISODES_ID:
        return Episodes.CONTENT_ITEM_TYPE;
      case PLAYLIST:
        return Playlist.CONTENT_TYPE;
      case PLAYLIST_ID:
        return Playlist.CONTENT_ITEM_TYPE;
      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    final int match = sUrlMatcher.match(uri);
    switch (match) {
      case PODCASTS:
        db.insert(Tables.PODCASTS, null, values);
        notifyChange(uri);
        return Podcasts.buildPodcastUri(values.getAsString(Podcasts.PODCAST_ID));
      case EPISODES:
        Logger.d(LOG_TAG, "insert " + values.toString());
        db.insert(Tables.EPISODES, null, values);
        notifyChange(uri);
        return Episodes.buildEpisodeUri(values.getAsString(Episodes.EPISODE_ID));
      case PLAYLIST:
        db.insert(Tables.PLAYLIST, null, values);
        notifyChange(uri);
        return Playlist.CONTENT_URI;
      default:
        throw new UnsupportedOperationException("Unknown insert uri: " + uri);
    }
  }

  private void notifyChange(Uri uri) {
    getContext().getContentResolver().notifyChange(uri, null);
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    final SelectionBuilder builder = buildSimpleSelection(uri);
    final int match = sUrlMatcher.match(uri);
    int retVal = builder.where(selection, selectionArgs).delete(db);
    if (retVal > 0) {
      notifyChange(uri);
    }
    return retVal;
  }

  private SelectionBuilder buildSimpleSelection(Uri uri) {
    final SelectionBuilder builder = new SelectionBuilder();
    final int match = sUrlMatcher.match(uri);
    switch (match) {
      case PODCASTS:
        return builder.table(Tables.PODCASTS);
      case PODCASTS_ID:
        final String podcastId = Podcasts.getPodcastId(uri);
        return builder.table(Tables.PODCASTS)
            .where(Podcasts.PODCAST_ID + "=?", podcastId);
      case EPISODES:
        return builder.table(Tables.EPISODES);
      case EPISODES_ID:
        final String episodeId = Episodes.getEpisodeId(uri);
        return builder.table(Tables.EPISODES)
            .where(Episodes.EPISODE_ID + "=?", episodeId);
      case PLAYLIST:
        return builder.table(Tables.PLAYLIST);
      case PLAYLIST_ID_EPISODES:
        Logger.e(LOG_TAG, "returning table playlist_on_episodes");
        return builder.table(Tables.PLAYLIST_ON_EPISODES);
      default:
        throw new UnsupportedOperationException("Unknown uri " + uri);
    }
  }

  private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
    final SelectionBuilder builder = new SelectionBuilder();
    Logger.e(LOG_TAG, "buildExpandedSelection: match: " + match);
    Logger.e(LOG_TAG, "buildExpandedSelection: uri: " + uri.toString());
    switch (match) {
      case PODCASTS:
        return builder.table(Tables.PODCASTS);
      case PODCASTS_ID:
        final String podcastId = Podcasts.getPodcastId(uri);
        return builder.table(Tables.PODCASTS)
            .where(Podcasts.PODCAST_ID + "=?", podcastId);
      case EPISODES:
        return builder.table(Tables.EPISODES);
      case EPISODES_ID:
        final String episodeId = Episodes.getEpisodeId(uri);
        return builder.table(Tables.EPISODES)
            .where(Episodes.EPISODE_ID + "=?", episodeId);
      case EPISODES_ON_PODCAST:
        final String episodePodcastId = Podcasts.getPodcastId(uri);
        return builder.table(Tables.EPISODES)
            .where(Podcasts.PODCAST_ID + "=?", episodePodcastId);
      case PLAYLIST:
        return builder.table(Tables.PLAYLIST);
      case PLAYLIST_ID_EPISODES:
        Logger.e(LOG_TAG, "returning table playlist_on_episodes");
        return builder.table(Tables.PLAYLIST_ON_EPISODES);
      default:
        throw new UnsupportedOperationException("Unknown uri " + uri);
    }
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    final SelectionBuilder builder = buildSimpleSelection(uri);
    int retVal = builder.where(selection, selectionArgs).update(db, values);
    notifyChange(uri);
    return retVal;
  }
}
