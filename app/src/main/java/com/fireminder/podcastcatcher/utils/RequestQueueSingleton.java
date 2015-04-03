package com.fireminder.podcastcatcher.utils;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Helper class for Volley's RequestQueue implementation.
 */
public class RequestQueueSingleton {
  private static RequestQueueSingleton mInstance;
  private RequestQueue mRequestQueue;
  private static Context mContext;

  private RequestQueueSingleton(Context context) {
    mContext = context;
    mRequestQueue = getRequestQueue();
  }

  public static synchronized RequestQueueSingleton getInstance(Context context) {
    if (mInstance == null) {
      mInstance = new RequestQueueSingleton(context);
    }
    return mInstance;
  }

  public RequestQueue getRequestQueue() {
    if (mRequestQueue == null) {
      mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
    }
    return mRequestQueue;
  }

  public <T> void addToRequestQueue(Request<T> request) {
    getRequestQueue().add(request);
  }
}
