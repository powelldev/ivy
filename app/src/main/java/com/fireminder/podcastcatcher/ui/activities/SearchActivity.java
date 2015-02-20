package com.fireminder.podcastcatcher.ui.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.ui.fragments.SearchFragment;

public class SearchActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        SearchFragment fragment = new SearchFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_upper,
                fragment, "search").commit();
    }
}
