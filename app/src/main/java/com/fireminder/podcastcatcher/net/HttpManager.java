package com.fireminder.podcastcatcher.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.fireminder.podcastcatcher.IvyApplication;
import com.fireminder.podcastcatcher.utils.Logger;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class HttpManager {

  private static final String TAG = "HttpManager";

  public long getTargetFileSize(String streamUri) {
    try {
      URL url = new URL("http://server.com/file.mp3");
      URLConnection urlConnection = url.openConnection();
      urlConnection.connect();
      return urlConnection.getContentLength();
    } catch (IOException e) {
      Logger.e(TAG, "getTargetFileSize: ", e);
      return -1;
    }
  }

  public boolean hasInternet() {
      ConnectivityManager connectivityManager
          = (ConnectivityManager) IvyApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
      return activeNetworkInfo != null && activeNetworkInfo.isConnected();
  }
}
