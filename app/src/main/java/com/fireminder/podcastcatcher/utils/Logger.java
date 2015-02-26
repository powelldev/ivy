package com.fireminder.podcastcatcher.utils;

public class Logger {
  private static final String LOG_TAG = "PodcastCatcher ";

  public static String makeLogTag(Class cls) {
    return cls.getSimpleName();
  }

  public static void e(String source, String message) {
    android.util.Log.e(LOG_TAG + source, message);
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
}
