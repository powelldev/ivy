package com.fireminder.podcastcatcher.utils;

import com.fireminder.podcastcatcher.BuildConfig;

import java.io.IOException;

public class Logger {
  private static final String LOG_TAG = "PodcastCatcher ";

  public static String makeLogTag(Class cls) {
    return cls.getSimpleName();
  }

  public static void e(String source, String message) {
    android.util.Log.e(LOG_TAG + source, message);
  }

  public static void e(String source, String message, Throwable tr) {
    android.util.Log.e(LOG_TAG + source, message, tr);
  }

  public static void i(String source, String message) {
    android.util.Log.i(LOG_TAG + source, message);
  }

  public static void d(String source, String message) {
    android.util.Log.d(LOG_TAG + source, message);
  }

  public static void w(String source, String message) {
    android.util.Log.w(LOG_TAG + source, message);
  }

  /**
   * Something has occurred that isn't too terrible. Only crash on debug builds.
   */
  public static void assertOrError(String tag, String s) {
    if (BuildConfig.DEBUG) {
      throw new AssertionError(s);
    }
    android.util.Log.e(LOG_TAG + s, s);
  }

  public static void assertOrError(String tag, String s, IOException e) {
    if (BuildConfig.DEBUG) {
      throw new AssertionError(s, e);
    }
    android.util.Log.e(LOG_TAG + s, s, e);
  }
}
