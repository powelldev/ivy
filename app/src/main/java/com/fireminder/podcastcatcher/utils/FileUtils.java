package com.fireminder.podcastcatcher.utils;

import android.content.Context;

import java.io.File;

public class FileUtils {

  private static File getBaseDir(Context context) {
    return context.getFilesDir();
  }

  public static File createFileIfNotExists(Context context, String filename) {
    return new File(getBaseDir(context), filename);
  }

}
