package com.fireminder.podcastcatcher.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.fireminder.podcastcatcher.IvyApplication;

import java.util.HashSet;
import java.util.Set;

/**
 * Responsible for managing access to SharedPreferences items
 */
public class IvyPreferences {

  private static final String PREF_PODCAST_PLAYING = "pref_podcast_playing";
  private static final String PREF_EPISODE_PLAYING = "pref_episode_playing";
  private static final String PREF_PREFETCH_NUM = "pref_prefetch_num";
  private static final String PREF_MOBILE_ALLOWED= "pref_mobile_allowed";
  private static final String PREF_SORTING_ASCENDING = "pref_sorting_ascending";
  private static final String PREF_DELETE_OLD = "pref_delete_old";
  private static final String PREF_DOWNLOAD_ID = "pref_download_id";
  private static final String PREF_DOWNLOADING_ITEMS = "pref_downloading_item";

  private SharedPreferences pref() {
    return PreferenceManager.getDefaultSharedPreferences(IvyApplication.getAppContext());
  }
  public Set<String> getDownloadingItems() {
    return pref().getStringSet(PREF_DOWNLOADING_ITEMS, new HashSet<String>());
  }

  public void setDownloadingItems(Set<String> items) {
    pref().edit().putStringSet(PREF_DOWNLOADING_ITEMS, items).apply();
  }


  public void setPodcastPlaying(String podcastId) {
    pref().edit().putString(PREF_PODCAST_PLAYING, podcastId).apply();
  }

  public String getPodcastPlaying() {
    return pref().getString(PREF_PODCAST_PLAYING, "");
  }

  public void setEpisodePlaying(String episodeId) {
    pref().edit().putString(PREF_EPISODE_PLAYING, episodeId).apply();
  }

  public String getEpisodePlaying() {
    return pref().getString(PREF_EPISODE_PLAYING, "");
  }

  public int getNumEpisodesToPrefetch() {
    return pref().getInt(PREF_PREFETCH_NUM, 3);
  }

  public boolean isMobileAllowed() {
    return pref().getBoolean(PREF_MOBILE_ALLOWED, true);
  }

  public void setSortAscending(boolean ascending) {
    pref().edit().putBoolean(PREF_SORTING_ASCENDING, ascending).apply();
  }

  public String isSortingAscending() {
    return pref().getBoolean(PREF_SORTING_ASCENDING, true) ? " ASC" : " DESC";
  }

  public boolean isDeletingOld() {
    return pref().getBoolean(PREF_DELETE_OLD, false);
  }

  /**
   * Download ids must be unique. This will return a new int and prepare the next.
   */
  public int getNextDownloadId() {
    int id =  pref().getInt(PREF_DOWNLOAD_ID, 0);
    pref().edit().putInt(PREF_DOWNLOAD_ID, ++id).apply();
    return id;
  }
}
