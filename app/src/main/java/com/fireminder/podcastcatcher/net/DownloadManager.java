package com.fireminder.podcastcatcher.net;

import android.net.Uri;

import com.fireminder.podcastcatcher.IvyApplication;
import com.fireminder.podcastcatcher.models.Episode;
import com.fireminder.podcastcatcher.models.EpisodeModel;
import com.fireminder.podcastcatcher.utils.Logger;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import java.io.File;
import java.io.IOException;

public class DownloadManager {


  private static final String TAG = "DownloadManager";

  private final FileManager fileManager;
  private final EpisodeModel episodeModel;
  private final HttpManager httpManager;

  public DownloadManager(FileManager fileManager, EpisodeModel episodeModel,
                         HttpManager httpManager) {
    this.fileManager = fileManager;
    this.episodeModel = episodeModel;
    this.httpManager = httpManager;
  }

  public interface DownloadCallback {
    void onProgress(long downloaded, long total);
    void onCompleted(Exception e, File file);
    void onError(Exception e);
    void onNoInternet();
  }

  public void download(final Episode episode,
                       final DownloadCallback callback) {

    if (!httpManager.hasInternet()) {
      callback.onNoInternet();
      return;
    }

    if (episode.downloadStatus == Episode.DownloadStatus.DOWNLOAD_IN_PROGRESS) {
      Logger.i(TAG, "Episode : " + episode.title + " download in progress.");
      return;
    }

    final Uri filename;
    try {

      filename = fileManager.createFilename(episode.streamUri,
          episodeModel.getPodcast(episode).title);

    } catch (IOException e) {
      Logger.assertOrError(TAG, "Failed to create directory: ", e);
      return;
    }

    if (fileManager.exists(filename)
        && httpManager.getTargetFileSize(episode.streamUri) == fileManager.getFileSize(filename)) {
      Logger.i(TAG, "download(): " + episode.title + " exists locally. Skipping download");
      episodeModel.updateDownloadedStatus(episode, Episode.DownloadStatus.DOWNLOADED, filename);
      File file = new File(filename.toString());
      callback.onCompleted(null, file);
      return;
    }

    File destination;
    try {
      destination = fileManager.createFile(filename);
    } catch (IOException e) {
      episodeModel.updateDownloadedStatus(episode, Episode.DownloadStatus.DOWNLOAD_INTERRUPTED, null);
      callback.onError(e);
      Logger.assertOrError(TAG, "failed to create file destination", e);
      return;
    }

    Ion.with(IvyApplication.getAppContext())
        .load(episode.streamUri)
        .progress(new ProgressCallback() {
          @Override
          public void onProgress(long downloaded, long total) {
            callback.onProgress(downloaded, total);
          }
        })
        .write(destination)
        .setCallback(new FutureCallback<File>() {
          @Override
          public void onCompleted(Exception e, File file) {
            callback.onCompleted(e, file);
          }
        });

  }

}
