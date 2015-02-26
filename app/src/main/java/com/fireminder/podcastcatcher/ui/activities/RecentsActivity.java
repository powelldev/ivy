package com.fireminder.podcastcatcher.ui.activities;

import android.os.Bundle;

import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.ui.fragments.RecentsFragment;

public class RecentsActivity extends BaseActivity {

  private static final String LOG_TAG = RecentsFragment.class.getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_channel);
    setTitle(R.string.title_activity_recents);
    RecentsFragment fragment = RecentsFragment.newInstance();
    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_upper,
        fragment, "recents").commit();
  }


}
