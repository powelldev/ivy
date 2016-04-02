package com.fireminder.podcastcatcher.net;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.fireminder.podcastcatcher.IvyApplication;
import com.fireminder.podcastcatcher.utils.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.annotation.Nullable;

public class FileManager {

  public Uri createFilename(@NonNull final String streamUri, @Nullable String dirName) throws IOException {
    File baseDir;
    if (TextUtils.isEmpty(dirName)) {
      baseDir = IvyApplication.getAppContext().getExternalFilesDir(null);
    } else {
      createDirIfNotExists(dirName);
      baseDir = new File(IvyApplication.getAppContext().getExternalFilesDir(null), dirName);
    }
    return Uri.withAppendedPath(Uri.fromFile(baseDir), FilenameUtils.getBaseName(streamUri) + FilenameUtils.getExtension(streamUri));
  }

  private void createDirIfNotExists(String dirName) throws IOException {
    File baseDir = new File(IvyApplication.getAppContext().getExternalFilesDir(null), dirName);
    baseDir.mkdir();
  }

  public boolean exists(@NonNull Uri filename) {
    return new File(filename.getPath()).exists();
  }

  public long getFileSize(Uri filename) {
    File file = new File(URI.create(filename.toString()));
    return file.length();
  }

  public File createFile(Uri filename) throws IOException {
    File file = new File(URI.create(filename.toString()));
    file.createNewFile();
    return file;
  }

}
