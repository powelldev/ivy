package com.fireminder.podcastcatcher.utils;

public class Constants {

  public interface Fragment {
    public static final String PODCASTS = "podcasts";
  }

  public interface NavDrawerItems {
    public static final String RECENTLY_ADDED = "Recently Added";
    public static final String PODCAST_CHANNELS = "Podcast Channels";

    public static final String[] NAV_DRAWER_ITEMS = new String[] {
        RECENTLY_ADDED, PODCAST_CHANNELS
    };
  }

}
