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

public class EpisodeTest extends TestCase {

  List<String> responses = new ArrayList<>();

  @Override
  public void setUp() throws IOException {
    List<URL> urls = new ArrayList<>();

    urls.add(new URL("http://feeds.feedburner.com/dancarlin/history?format=xml")); // normal rss
    urls.add(new URL("http://www.theskepticsguide.org/feed/sgu")); // has BOM issue
    //urls.add(new URL("http://www.fragmentedpodcast.com/feed")); // Use correct correcting regex.

    for (int i = 0; i < urls.size(); i++) {
      responses.add(CharStreams.toString(new InputStreamReader(((HttpURLConnection) urls.get(i).openConnection()).getInputStream(), Charsets.UTF_8)));
    }
  }
  public void testParseEpisodeFromCursor() throws Exception {

  }

  public void testParseEpisodesFromResponse() throws Exception {
    for (String response : responses) {
      List<Episode> episodes = Episode.parseEpisodesFromResponse(response);
      for (Episode episode : episodes) {
        assertNotNull(episode);
        assertNotNull(episode.title);
        assertNotNull(episode.description);
        assertNotNull(episode.pubDate);
        assertNotNull(episode.streamUri);
        assertEquals(episode.isDownloaded, false);
        assertEquals(episode.duration, 0);
        assertEquals(episode.elapsed, 0);
        assertEquals(episode.localUri, "");
      }
    }
  }

  public void testWriteToParcel() throws Exception {

  }

  public void testEpisodeToContentValues() throws Exception {

  }

  public void testGetUri() throws Exception {

  }
}