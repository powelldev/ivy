package com.fireminder.podcastcatcher.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class PodcastCatcherContract {

  public static final String CONTENT_AUTHORITY = "com.fireminder.podcastcatcher";
  public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
  private static final String PATH_PODCASTS = "podcasts";
  private static final String PATH_EPISODES = "episodes";
  private static final String PATH_PLAYLIST = "playlist";

  public PodcastCatcherContract() {
  }

  interface PodcastColumns {
    String PODCAST_ID = "podcastId";
    String PODCAST_TITLE = "podcast_title";
    String PODCAST_DESCRIPTION = "podcast_description";
    String PODCAST_FEED = "podcast_feed";
    String PODCAST_IMAGEURL = "podcast_imageUrl";
  }

  interface EpisodeColumns {
    String EPISODE_ID = "episodeId";
    String EPISODE_TITLE = "episode_title";
    String EPISODE_DESCRIPTION = "episode_description";
    String EPISODE_STREAM_URI = "episode_stream_uri";
    String EPISODE_LOCAL_URI = "episode_local_uri";
    String EPISODE_PUBLICATION_DATE = "episode_publication_date";
    String EPISODE_CONTENT_DURATION = "episode_content_duration";
    String EPISODE_PERCENT_ELAPSED = "episode_percent_elapsed";
    String EPISODE_DOWNLOAD_STATUS = "episode_download_status";
    String EPISODE_IS_COMPLETE = "episode_complete";
  }

  public static class Podcasts implements PodcastColumns, BaseColumns {
    public static final Uri CONTENT_URI =
        BASE_CONTENT_URI.buildUpon().appendPath(PATH_PODCASTS).build();

    public static final String CONTENT_TYPE =
        "vnd.android.cursor.dir/vnd.podcastcatcher.podcast";

    public static final String CONTENT_ITEM_TYPE =
        "vnd.android.cursor.item/vnd.podcastcatcher.podcast";

    public static Uri buildPodcastUri(String podcastId) {
      return CONTENT_URI.buildUpon().appendPath(podcastId).build();
    }

    public static String getPodcastId(Uri uri) {
      return uri.getPathSegments().get(1);
    }

  }

  public static class Episodes implements EpisodeColumns, BaseColumns {
    public static final Uri CONTENT_URI =
        BASE_CONTENT_URI.buildUpon().appendPath(PATH_EPISODES).build();

    public static final String CONTENT_TYPE =
        "vnd.android.cursor.dir/vnd.podcastcatcher.episode";

    public static final String CONTENT_ITEM_TYPE =
        "vnd.android.cursor.item/vnd.podcastcatcher.episode";

    public static Uri buildEpisodeUri(String episodeId) {
      return CONTENT_URI.buildUpon().appendPath(episodeId).build();
    }

    public static String getEpisodeId(Uri uri) {
      return uri.getPathSegments().get(1);
    }

  }

}

