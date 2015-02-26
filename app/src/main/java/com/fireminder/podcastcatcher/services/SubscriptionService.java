package com.fireminder.podcastcatcher.services;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.fireminder.podcastcatcher.models.Podcast;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract.Podcasts;
import com.fireminder.podcastcatcher.utils.Logger;
import com.fireminder.podcastcatcher.utils.RequestQueueSingleton;

import static com.android.volley.Response.ErrorListener;


public class SubscriptionService extends Service implements Response.Listener<String>, ErrorListener {

  private static final String LOG_TAG = SubscriptionService.class.getSimpleName();

  private static final String ACTION_SUBSCRIBE = "com.fireminder.podcastcatcher.services.action.SUBSCRIBE";

  private static final String EXTRA_TARGET_URL = "com.fireminder.podcastcatcher.services.extra.EXTRA_TARGET_URL";

  private String mTargetUrl;

  public static void launchSubscriptionService(Context context, String targetUrl) {
    Intent intent = new Intent(context, SubscriptionService.class);
    intent.setAction(ACTION_SUBSCRIBE);
    intent.putExtra(EXTRA_TARGET_URL, targetUrl);
    context.startService(intent);
  }

  public static void addEpisodes(Context context, String targetUrl) {
    Intent intent = new Intent(context, SubscriptionService.class);
    intent.setAction(ACTION_SUBSCRIBE);
    intent.putExtra(EXTRA_TARGET_URL, targetUrl);
    context.startService(intent);
  }


  @Override
  public IBinder onBind(Intent intent) {
    // Not a class that should be bound.
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null) {
      final String action = intent.getAction();
      if (ACTION_SUBSCRIBE.equals(action)) {
        mTargetUrl = intent.getStringExtra(EXTRA_TARGET_URL);
        handleSubscription();
      }
    }
    return Service.START_STICKY;
  }

  private void handleSubscription() {
    Logger.i(LOG_TAG, "handleSubscription: " + mTargetUrl);
    StringRequest request = new StringRequest(mTargetUrl, this, this);
    RequestQueueSingleton.getInstance(this).addToRequestQueue(request);
  }

  private void handleResponse(String response) {
    try {
      Podcast podcast = Podcast.parsePodcast2(response);

      ContentValues podcastValues = new ContentValues();
      podcast.feed = mTargetUrl;
      Logger.i(LOG_TAG, "handleSubscriptionResponse: " + podcast.toString());
      podcastValues.put(Podcasts.PODCAST_ID, podcast.podcastId);
      podcastValues.put(Podcasts.PODCAST_TITLE, podcast.title);
      podcastValues.put(Podcasts.PODCAST_DESCRIPTION, podcast.description);
      podcastValues.put(Podcasts.PODCAST_FEED, podcast.feed);
      podcastValues.put(Podcasts.PODCAST_IMAGEURL, podcast.imagePath);
      getContentResolver().insert(Podcasts.CONTENT_URI, podcastValues);
      RetrieveEpisodeService.startActionRetrieveAll(getApplicationContext(), podcast);
    } catch (Exception e) {
      Logger.e(LOG_TAG, "handleSubscriptionResponse: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public void onErrorResponse(VolleyError volleyError) {
    Logger.e(LOG_TAG, "onErrorResponse: " + volleyError.getMessage());
    volleyError.printStackTrace();
  }

  @Override
  public void onResponse(String response) {
    handleResponse(response);
    Logger.d(LOG_TAG, "onResponse");
  }
}
