package com.fireminder.podcastcatcher.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Responsible for managing access to SharedPreferences items
 */
public class PrefUtils {

  private static final String PREF_PODCAST_PLAYING = "pref_podcast_playing";

  public static void setPodcastPlaying(Context context, String podcastId) {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    sp.edit().putString(PREF_PODCAST_PLAYING, podcastId).apply();
  }

  public static String getPodcastPlaying(Context context) {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    return sp.getString(PREF_PODCAST_PLAYING, "");
  }

}
