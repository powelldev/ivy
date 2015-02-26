package com.fireminder.podcastcatcher.ui.activities;

import android.os.Bundle;

import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.ui.fragments.PlaylistFragment;

public class PlaylistActivity extends BaseActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_channel);
    setTitle(R.string.title_activity_playlist);
    PlaylistFragment fragment = PlaylistFragment.newInstance();
    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_upper,
        fragment, "playlists").commit();
  }

}