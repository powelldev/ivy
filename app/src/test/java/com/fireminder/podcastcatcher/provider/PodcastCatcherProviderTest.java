package com.fireminder.podcastcatcher.provider;

import android.content.ContentResolver;
import android.test.AndroidTestCase;
import android.test.ProviderTestCase2;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class PodcastCatcherProviderTest extends TestCase{
  private ContentResolver mContentResolver;
  private PodcastCatcherProvider mPodcastProvider;


  @Test
  public void test() {
    assertThat(Robolectric.application, notNullValue());
  }

}