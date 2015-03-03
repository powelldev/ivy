package com.fireminder.podcastcatcher;

import android.test.ActivityInstrumentationTestCase2;

import com.fireminder.podcastcatcher.ui.activities.PodcastsActivity;

public class HelloWorldEspressoTest extends ActivityInstrumentationTestCase2<PodcastsActivity>{

  public HelloWorldEspressoTest() {
    super(PodcastsActivity.class);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    getActivity();
  }
}
