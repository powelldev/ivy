package com.fireminder.podcastcatcher.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;

import java.util.ArrayList;
import java.util.List;

public class Playlist {

  public List<Episode> episodes;

  public static Playlist parsePlaylistFromCursor(Cursor cursor) {
    Playlist playlist = new Playlist();
    playlist.episodes = new ArrayList<>();
    while (cursor.moveToNext()) {
      playlist.episodes.add(Episode.parseEpisodeFromCursor(cursor));
    }
    return playlist;
  }

  public static List<ContentValues> playlistToContentValues(Playlist playlist) {
    List<ContentValues> cvs = new ArrayList<>();
    for (int i = 0; i < playlist.episodes.size(); i++) {
      ContentValues cv = new ContentValues();
      cv.put(PodcastCatcherContract.Playlist.PLAYLIST_ORDER, i);
      cv.put(PodcastCatcherContract.Episodes.EPISODE_ID, playlist.episodes.get(i).episode_id);
      cvs.add(cv);
    }
    return cvs;
  }

  public static int getItemCount(Context context) {
    Cursor c = context.getContentResolver().query(PodcastCatcherContract.Playlist.CONTENT_URI,
        null,
        null,
        null,
        null);
    int count = c.getCount();
    c.close();
    return count;
  }
}
