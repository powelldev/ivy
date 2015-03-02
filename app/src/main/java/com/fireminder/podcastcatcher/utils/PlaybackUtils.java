package com.fireminder.podcastcatcher.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.fireminder.podcastcatcher.models.Episode;
import com.fireminder.podcastcatcher.models.Podcast;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;
import com.fireminder.podcastcatcher.services.DownloadManagerService;

/**
 * Created by powelldev on 3/1/15.
 */
public class PlaybackUtils {
  public static void downloadAllEpisodes(Context context, Podcast podcast) {
    Cursor cursor = context.getContentResolver().query(
        PodcastCatcherContract.Episodes.CONTENT_URI,
        null, /* projection */
        PodcastCatcherContract.Podcasts.PODCAST_ID + "=?",
        new String[]{podcast.podcastId},
        null /* sortOrder */);

    while (cursor.moveToNext()) {
      Episode episode = Episode.parseEpisodeFromCursor(cursor);
      DownloadManagerService.download(context, episode);
    }

  }

  public static String getEpisodeImage(Context context, Episode media) {
    Podcast podcast = getPodcastOf(context, media);
    return podcast.imagePath;
  }

  public static void updateEpisodeElapsed(Context context, Episode media, long currentPosition) {
    media.elapsed = currentPosition;
    ContentValues cv = Episode.episodeToContentValues(media);
    context.getContentResolver().update(
        PodcastCatcherContract.Episodes.buildEpisodeUri(media.episodeId),
        cv,
        null /* where */,
        null /* selectionArgs */
    );
  }

  public static Podcast getPodcastOf(Context context, Episode episode) {
    Cursor cursor = context.getContentResolver().query(PodcastCatcherContract.Podcasts.buildPodcastUri(episode.podcastId),
        null,
        null,
        null,
        null
    );

    cursor.moveToNext();
    Podcast podcast = Podcast.parsePodcastFromCursor(cursor);
    return podcast;
  }

  public static void setEpisodeComplete(Context context, Episode episode) {
    episode.isComplete = true;
    episode.elapsed = 0;
    ContentValues cv = Episode.episodeToContentValues(episode);
    context.getContentResolver().update(
        PodcastCatcherContract.Episodes.buildEpisodeUri(episode.episodeId),
        cv,
        null /* where */,
        null /* selectionArgs */
    );
  }

  public static void downloadNextXEpisodes(Context context, Podcast podcast, int numToDownload) {
    Cursor cursor = context.getContentResolver().query(PodcastCatcherContract.Episodes.CONTENT_URI,
        null,
        PodcastCatcherContract.Podcasts.PODCAST_ID + "=?",
        new String[]{podcast.podcastId},
        null
    );

    while (cursor.moveToNext() && numToDownload > 0) {
      Episode episode = Episode.parseEpisodeFromCursor(cursor);
      if (!episode.isComplete) {
        DownloadManagerService.download(context, episode);
        numToDownload--;
      }
    }
  }

  public static Episode getNextEpisode(Context mContext, Podcast podcast) {
    Cursor cursor = mContext.getContentResolver().query(PodcastCatcherContract.Episodes.CONTENT_URI,
        null,
        PodcastCatcherContract.Podcasts.PODCAST_ID + "=?",
        new String[]{podcast.podcastId},
        null
    );

    while (cursor.moveToNext()) {
      Episode episode = Episode.parseEpisodeFromCursor(cursor);
      if (!episode.isComplete) {
        return episode;
      }
    }

    return null;
  }
}
