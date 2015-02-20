package com.fireminder.podcastcatcher.ui.activities;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.ui.fragments.ChannelFragment;
import com.fireminder.podcastcatcher.utils.Logger;

public class ChannelActivity extends BaseActivity {


    private static final String LOG_TAG = ChannelActivity.class.getSimpleName();
    public static final String EXTRA_PODCAST_ID = "extra_podcast_id";
    public static final String EXTRA_PODCAST_TITLE = "podcast_title";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        String podcastId = getIntent().getExtras().getString(EXTRA_PODCAST_ID);
        String podcastTitle = getIntent().getExtras().getString(EXTRA_PODCAST_TITLE);
        ChannelFragment fragment = ChannelFragment.newInstance(podcastId, podcastTitle);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_upper,
                fragment, "channel").commit();

    }

}
