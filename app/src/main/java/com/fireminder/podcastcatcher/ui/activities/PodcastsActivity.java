package com.fireminder.podcastcatcher.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.models.Podcast;
import com.fireminder.podcastcatcher.ui.fragments.PodcastsFragment;
import com.fireminder.podcastcatcher.utils.Logger;

public class PodcastsActivity extends BaseActivity implements PodcastsFragment.Listener {

    private static final String LOG_TAG = PodcastsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PodcastsFragment fragment = PodcastsFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_upper,
                fragment, "podcasts").commit();

    }

    @Override
    public void onPodcastItemInteraction(Podcast podcast) {
        Intent i = new Intent(this, ChannelActivity.class);
        i.putExtra(ChannelActivity.EXTRA_PODCAST_ID, podcast.podcastId);
        i.putExtra(ChannelActivity.EXTRA_PODCAST_TITLE, podcast.title);
        startActivity(i);
    }
}
