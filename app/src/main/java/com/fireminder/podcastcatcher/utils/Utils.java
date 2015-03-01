package com.fireminder.podcastcatcher.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;

import com.fireminder.podcastcatcher.models.Episode;
import com.fireminder.podcastcatcher.models.Podcast;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;
import com.fireminder.podcastcatcher.services.DownloadManagerService;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;

/**
 * Created by michael on 2/16/2015.
 */
public class Utils {
  public static final String DL_PREF = "downloadPreferences";

  public static boolean isOnline(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    return cm.getActiveNetworkInfo() != null &&
        cm.getActiveNetworkInfo().isConnected();
  }

  public static String validateXml(String xmlResponse) {
    // Some feeds can contain a Byte Order mark before xml begins
    // this regex removes those before parsing.
    return xmlResponse.replaceAll("^[^<]*<", "<");
  }
  public static String removeIncompleteFromFilename(String local_uri) {
    return local_uri.replace(".INCMPL", "");
  }

  public static String createIncompleteFilename(Context applicationContext, String episode_id) {
    File file = new File(applicationContext.getExternalFilesDir(null), episode_id + ".INCMPL");
    return file.getAbsolutePath();
  }

  public static void copyFile(String from, String to) throws IOException {
    FileChannel in = null;
    FileChannel out = null;
    try {
      in = new FileInputStream(new File(from)).getChannel();
      out = new FileOutputStream(new File(from)).getChannel();
      in.transferTo(0, in.size(), out);
      new File(from).delete();
    } finally {
      if (in != null) {
        in.close();
      }
      if (out != null) {
        out.close();
      }
    }

  }

  public static void downloadAllEpisodes(Context context, Podcast podcast) {
    Cursor cursor = context.getContentResolver().query(
        PodcastCatcherContract.Episodes.CONTENT_URI,
        null, /* projection */
        PodcastCatcherContract.Podcasts.PODCAST_ID + "=?",
        new String[] {podcast.podcastId},
        null /* sortOrder */);

    while (cursor.moveToNext()) {
      Episode episode = Episode.parseEpisodeFromCursor(cursor);
      DownloadManagerService.download(context, episode);
    }

  }

  public static String makeTimePretty(long pubDate) {
    Date date = new Date(pubDate);
    Date oneYearAgo = new Date(System.currentTimeMillis());
    PrettyTime prettyTime = new PrettyTime();
    return prettyTime.format(new Date(pubDate));
  }

  public static Uri createEpisodeDestination(Context applicationContext, String episodeId) {
    return Uri.withAppendedPath(Uri.fromFile(applicationContext.getExternalFilesDir(null)),
        episodeId);
  }

  public static Episode getNextEpisode(Context mContext, Podcast podcast) {
    Cursor cursor = mContext.getContentResolver().query(PodcastCatcherContract.Episodes.CONTENT_URI,
        null,
        PodcastCatcherContract.Podcasts.PODCAST_ID + "=?",
        new String[] {podcast.podcastId},
        null
    );

    while(cursor.moveToNext()) {
      Episode episode = Episode.parseEpisodeFromCursor(cursor);
      if (!episode.isComplete) {
        return episode;
      }
    }

    return null;
  }
}
