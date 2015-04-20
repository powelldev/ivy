package com.fireminder.podcastcatcher.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.fireminder.podcastcatcher.models.Episode;
import com.fireminder.podcastcatcher.models.Podcast;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;
import com.fireminder.podcastcatcher.services.DownloadManagerService;

import java.io.File;

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
    try {
      Cursor cursor = context.getContentResolver().query(PodcastCatcherContract.Podcasts.buildPodcastUri(episode.podcastId),
          null,
          null,
          null,
          null
      );

      cursor.moveToNext();
      Podcast podcast = Podcast.parsePodcastFromCursor(cursor);
      return podcast;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static void setEpisodeComplete(Context context, Episode episode, boolean isComplete) {
    episode.isComplete = isComplete;
    episode.elapsed = 0;
    ContentValues cv = Episode.episodeToContentValues(episode);
    context.getContentResolver().update(
        PodcastCatcherContract.Episodes.buildEpisodeUri(episode.episodeId),
        cv,
        null /* where */,
        null /* selectionArgs */
    );
  }

  public static void downloadNextXEpisodes(final Context context, final Podcast podcast, final int numToDownload) {
    new Thread() {
      @Override
      public void run() {
        int numRemaining = numToDownload;
        String sort = PrefUtils.isSortingAscending(context);
        Cursor cursor = context.getContentResolver().query(PodcastCatcherContract.Episodes.CONTENT_URI,
            null,
            PodcastCatcherContract.Podcasts.PODCAST_ID + "=?",
            new String[]{podcast.podcastId},
            PodcastCatcherContract.Episodes.EPISODE_PUBLICATION_DATE + sort
        );

        while (cursor.moveToNext() && numRemaining > 0) {
          Episode episode = Episode.parseEpisodeFromCursor(cursor);
          if (!episode.isComplete) {
            DownloadManagerService.download(context, episode);
            numRemaining--;
          }
        }
      }
    }.start();

  }

  public static Episode getNextEpisode(Context mContext, Podcast podcast) {
    String sort = PrefUtils.isSortingAscending(mContext);
    Cursor cursor = mContext.getContentResolver().query(PodcastCatcherContract.Episodes.CONTENT_URI,
        null,
        PodcastCatcherContract.Podcasts.PODCAST_ID + "=?",
        new String[]{podcast.podcastId},
        PodcastCatcherContract.Episodes.EPISODE_PUBLICATION_DATE + sort
    );

    while (cursor.moveToNext()) {
      Episode episode = Episode.parseEpisodeFromCursor(cursor);
      if (!episode.isComplete) {
        return episode;
      }
    }

    return null;
  }

  public static Episode getPreviousEpisode(Context context, Podcast podcast, Episode episode) {
    String sort = PrefUtils.isSortingAscending(context);
    Cursor cursor = context.getContentResolver().query(PodcastCatcherContract.Episodes.CONTENT_URI,
        null,
        PodcastCatcherContract.Podcasts.PODCAST_ID + "=?",
        new String[]{podcast.podcastId},
        PodcastCatcherContract.Episodes.EPISODE_PUBLICATION_DATE + sort
    );

    Episode previousEpisode = null;
    while (cursor.moveToNext()) {
      Episode temp = Episode.parseEpisodeFromCursor(cursor);
      if (episode.episodeId.equals(temp.episodeId)) {
        return previousEpisode;
      } else {
        previousEpisode = temp;
      }
    }
    return null;
  }

  public static void clearOldEpisodes(Context context, Podcast podcast, Episode completed) {
    String sort = PrefUtils.isSortingAscending(context);
    Cursor cursor = context.getContentResolver().query(PodcastCatcherContract.Episodes.CONTENT_URI,
        null,
        PodcastCatcherContract.Podcasts.PODCAST_ID + "=?",
        new String[]{podcast.podcastId},
        PodcastCatcherContract.Episodes.EPISODE_PUBLICATION_DATE + sort
    );

    while (cursor.moveToNext()) {
      Episode episode = Episode.parseEpisodeFromCursor(cursor);
      if (episode.isComplete && new File(Uri.parse(episode.localUri).getPath()).exists()) {
        new File(Uri.parse(episode.localUri).getPath()).delete();
        episode.localUri = "";
        episode.isDownloaded = false;
        ContentValues cv = Episode.episodeToContentValues(episode);
        context.getContentResolver().update(PodcastCatcherContract.Episodes.buildEpisodeUri(episode.episodeId),
            cv,
            null,
            null);
      }
    }
  }
}
