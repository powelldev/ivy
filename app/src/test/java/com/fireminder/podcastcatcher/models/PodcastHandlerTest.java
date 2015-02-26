package com.fireminder.podcastcatcher.models;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PodcastHandlerTest extends TestCase {


  List<String> responses = new ArrayList<>();

  @Override
  public void setUp() throws IOException {
    List<URL> urls = new ArrayList<>();

    urls.add(new URL("http://feeds.feedburner.com/dancarlin/history?format=xml")); // normal rss
    urls.add(new URL("http://www.theskepticsguide.org/feed/sgu")); // has BOM issue
    //urls.add(new URL("http://www.fragmentedpodcast.com/feed")); // Use correct correcting regex.

    for (int i = 0; i < urls.size(); i++) {
      responses.add(CharStreams.toString(new InputStreamReader(((HttpURLConnection)urls.get(i).openConnection()).getInputStream(), Charsets.UTF_8)));
    }
  }

  public void testParsePodcast2() throws Exception {
    for (String response : responses) {
      Podcast podcast = Podcast.parsePodcast2(response);
      assertNotNull(podcast);
      assertNotNull(podcast.title);
      assertNotNull(podcast.description);
      assertNotNull(podcast.imagePath);
      assertNotNull(podcast.podcastId);
      assertNull(podcast.feed);
    }
  }
}