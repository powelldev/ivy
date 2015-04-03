package com.fireminder.podcastcatcher.net;

import android.content.Context;
import android.os.AsyncTask;

import com.fireminder.podcastcatcher.utils.FileUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import org.apache.http.Header;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DownloadQueueProcessor {

  private Context context;
  private List<DownloadTask> taskList = new ArrayList<>();
  private AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
  private DownloadTaskListener mDownloadTaskListener;


  public DownloadQueueProcessor(Context context) {
    this.context = context.getApplicationContext();
    asyncHttpClient.setTimeout(10000);
  }

  public void enqueue(DownloadTask task) {
    taskList.add(task);
  }

  public void process() {
    final Iterator<DownloadTask> iterator = taskList.iterator();
    if (!iterator.hasNext()) {
      return;
    }

    final DownloadTask task = iterator.next();
    final String fileName = task.uri.getLastPathSegment();

    File file = prepareDestination(fileName);

    asyncHttpClient.get(task.uri.toString(), new FileAsyncHttpResponseHandler(file) {

      private int totalSize;
      private static final int POLL_INCREMENT = 10000;
      private int poll = POLL_INCREMENT;

      @Override
      public void onProgress(int bytesWritten, int totalSize) {
        this.totalSize = totalSize;
        if (mDownloadTaskListener != null && (bytesWritten > poll)) {
          super.onProgress(bytesWritten, totalSize);
          poll += POLL_INCREMENT;
          mDownloadTaskListener.onProgress(bytesWritten, totalSize, task.id, task.type);
        }
      }

      @Override
      public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
        poll = POLL_INCREMENT;
        if (mDownloadTaskListener != null) {
          mDownloadTaskListener.onFailure(statusCode, throwable);
        }
      }

      @Override
      public void onSuccess(int statusCode, Header[] headers, File file) {
         poll = POLL_INCREMENT;
        if (iterator.hasNext()) {
          process();
        }
        else {
          if (mDownloadTaskListener != null) {
            mDownloadTaskListener.onSuccess(file, task.id);
          }
        }

      }
    });

  }

  public interface DownloadTaskListener {
    public void onSuccess(File file, int id);
    public void onProgress(int bytesWritten, int totalSize, int id, FileType type);
    public void onFailure(int statusCode, Throwable throwable);
  }

  public void setDownloadTaskListener(DownloadTaskListener listener) {
    mDownloadTaskListener = listener;
  }

  private File prepareDestination(String fileName) {
    return FileUtils.createFileIfNotExists(context, fileName);
  }

}
