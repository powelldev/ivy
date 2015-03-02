package com.fireminder.podcastcatcher.ui.activities;

import android.os.Bundle;

import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.ui.fragments.PodcastsFragment;

/**
 * Activity responsible for presenting PodcastFragment
 */
public class PodcastsActivity extends BaseActivity {

  private static final String LOG_TAG = PodcastsActivity.class.getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_upper,
        new PodcastsFragment(), "podcasts").commit();

  }

}
