package com.fireminder.podcastcatcher.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.fireminder.podcastcatcher.IvyApplication;
import com.fireminder.podcastcatcher.net.FileManager;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;
import com.fireminder.podcastcatcher.utils.Logger;
import com.google.common.io.CharStreams;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;

import static com.fireminder.podcastcatcher.provider.PodcastCatcherContract.Episodes.*;

public class EpisodeModel {

  private static final String TAG = "EpisodeModel";
  @Inject
  FileManager fileManager;

  private static final String LOG_TAG = "EpisodeModel";

  public List<Episode> getEpisodes(String url) {
    List<Episode> episodes = null;
    try {
      final InputStream is = new URL(url).openConnection().getInputStream();
      final String feed = CharStreams.toString(new InputStreamReader(is, "UTF-8"));
      episodes = Episode.parseEpisodesFromResponse(feed);
    } catch (Exception e) {
      Logger.e(LOG_TAG, "url: " + url + " Exception: " + e.getMessage());
      e.printStackTrace();
    }
    return episodes;
  }

  public void bulkInsert(List<Episode> episodes, String mPodcastId) {
    ContentValues[] contentValues = new ContentValues[episodes.size()];
    for (int i = 0; i < episodes.size(); i++) {
      ContentValues cv = Episode.episodeToContentValues(episodes.get(i));
      cv.put(PodcastCatcherContract.Podcasts.PODCAST_ID, mPodcastId);
      contentValues[i] = cv;
    }

    IvyApplication.getAppContext().getContentResolver()
        .bulkInsert(CONTENT_URI, contentValues);

  }

  public void updateDownloadedStatus(Episode episode,
                                     Episode.DownloadStatus status,
                                     Uri fileNameIfExists) {

    String filename = "";
    if (status == Episode.DownloadStatus.DOWNLOADED) {
      filename = fileNameIfExists.toString();
    }

    final ContentValues values = new ContentValues();
    values.put(EPISODE_DOWNLOAD_STATUS, status.id);
    values.put(EPISODE_LOCAL_URI, filename);
    IvyApplication.getAppContext().getContentResolver()
        .update(buildEpisodeUri(episode.episodeId), values, null, null);
  }

  public Podcast getPodcast(Episode episode) {
    Cursor cursor = IvyApplication.getAppContext()
        .getContentResolver()
        .query(PodcastCatcherContract.Podcasts.buildPodcastUri(episode.podcastId),
            null,
            null,
            null,
            null
        );

    Podcast podcast = null;
    if (cursor == null) {
      Logger.e(TAG, "No podcast for episode: " + episode.toString());
      return podcast;
    }
    try {

      cursor.moveToNext();
      podcast = Podcast.parsePodcastFromCursor(cursor);
    } finally {
      cursor.close();
    }

    return podcast;
  }
}
