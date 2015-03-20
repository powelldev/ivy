package com.fireminder.podcastcatcher.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Responsible for managing access to SharedPreferences items
 */
public class PrefUtils {

  private static final String PREF_PODCAST_PLAYING = "pref_podcast_playing";
  private static final String PREF_EPISODE_PLAYING = "pref_episode_playing";
  private static final String PREF_PREFETCH_NUM = "pref_prefetch_num";
  private static final String PREF_MOBILE_ALLOWED= "pref_mobile_allowed";
  private static final String PREF_SORTING_ASCENDING = "pref_sorting_ascending";
  private static final String PREF_DELETE_OLD = "pref_delete_old";

  public static void setPodcastPlaying(Context context, String podcastId) {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    sp.edit().putString(PREF_PODCAST_PLAYING, podcastId).apply();
  }

  public static String getPodcastPlaying(Context context) {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    return sp.getString(PREF_PODCAST_PLAYING, "");
  }

  public static void setEpisodePlaying(Context context, String episodeId) {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    sp.edit().putString(PREF_EPISODE_PLAYING, episodeId).apply();
  }

  public static String getEpisodePlaying(Context context) {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    return sp.getString(PREF_EPISODE_PLAYING, "");
  }
  public static int getNumEpisodesToPrefetch(Context context) {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    return sp.getInt(PREF_PREFETCH_NUM, 5);
  }

  public static boolean isMobileAllowed(Context context) {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    return sp.getBoolean(PREF_MOBILE_ALLOWED, false);
  }

  public static void setSortAscending(Context context, boolean ascending) {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    sp.edit().putBoolean(PREF_SORTING_ASCENDING, ascending).apply();
  }

  public static String isSortingAscending(Context context) {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    return sp.getBoolean(PREF_SORTING_ASCENDING, true) ? " ASC" : " DESC";
  }

  public static boolean isDeletingOld(Context context) {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    return sp.getBoolean(PREF_DELETE_OLD, false);
  }
}
