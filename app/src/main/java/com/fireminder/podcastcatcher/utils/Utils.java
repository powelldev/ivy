package com.fireminder.podcastcatcher.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Log;

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
import java.util.HashSet;

/**
 * Created by michael on 2/16/2015.
 */
@SuppressWarnings("ALL")
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
      boolean success = new File(from).delete();
    } finally {
      if (in != null) {
        in.close();
      }
      if (out != null) {
        out.close();
      }
    }

  }

  public static String makeTimePretty(long pubDate) {
    PrettyTime prettyTime = new PrettyTime();
    return prettyTime.format(new Date(pubDate));
  }

  public static Uri createEpisodeDestination(Context applicationContext, String episodeId) {
    return Uri.withAppendedPath(Uri.fromFile(applicationContext.getExternalFilesDir(null)),
        episodeId);
  }

  /**
   *  Over time, episode mp3s can get abandoned. Their podcast is deleted but they remain.
   * This should be run fairly regularly to delete these orphaned episodes
   */
  public static void cleanUpStorage(Context context) {
    // get all filenames in directory
    // for each filename, check if it exists in the database
    // if it does not, delete it
    HashSet hashSet = new HashSet();
    //Cursor cursor = context.getContentResolver().query(uri, projection, selection, args, sort);
    Cursor cursor = context.getContentResolver().query(PodcastCatcherContract.Episodes.CONTENT_URI,
        new String[] {PodcastCatcherContract.Episodes.EPISODE_ID},
        null,
        null,
        null);
    while (cursor.moveToNext()) {
      hashSet.add(cursor.getString(0));
    }
    File episodeDir = context.getExternalFilesDir(null);
    File[] episodes = episodeDir.listFiles();
    for (int i = 0; i < episodes.length; i++) {
      if (!hashSet.contains(episodes[i].getName())) {
        episodes[i].delete();
      }
    }
  }
}
