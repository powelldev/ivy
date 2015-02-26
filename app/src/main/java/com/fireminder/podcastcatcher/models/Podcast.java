package com.fireminder.podcastcatcher.models;

import android.database.Cursor;

import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;
import com.fireminder.podcastcatcher.utils.Utils;
import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class Podcast {
  public String podcastId;
  public String title;
  public String description;
  public String feed;
  public String imagePath;

  public static Podcast parsePodcastFromCursor(Cursor cursor) {
    Podcast podcast = new Podcast();
    podcast.title = cursor.getString(cursor.getColumnIndex(PodcastCatcherContract.Podcasts.PODCAST_TITLE));
    podcast.podcastId = cursor.getString(cursor.getColumnIndex(PodcastCatcherContract.Podcasts.PODCAST_ID));
    podcast.imagePath = cursor.getString(cursor.getColumnIndex(PodcastCatcherContract.Podcasts.PODCAST_IMAGEURL));
    podcast.description = cursor.getString(cursor.getColumnIndex(PodcastCatcherContract.Podcasts.PODCAST_DESCRIPTION));
    podcast.feed = cursor.getString(cursor.getColumnIndex(PodcastCatcherContract.Podcasts.PODCAST_FEED));
    return podcast;
  }

  public static Podcast parsePodcast2(String response) throws DocumentException {
    response = Utils.validateXml(response);

    Podcast podcast = new Podcast();
    Document document = DocumentHelper.parseText(response);
    Element rootElement = document.getRootElement();
    Element channel = rootElement.element("channel");
    podcast.title = channel.elementText("title");
    podcast.description = channel.elementText("description");
    podcast.imagePath = getImgPath(channel);
    podcast.podcastId = hash(podcast);

    return podcast;
  }

  private static String getImgPath(Element channel) {
    for (Element image : channel.elements("image")) {
      if ("itunes".equalsIgnoreCase(image.getNamespacePrefix())) {
        return image.attributeValue("href");
      }
    }
    return channel.element("thumbnail").attributeValue("url");
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(podcastId)
        .append("\n")
        .append(title)
        .append("\n")
        .append(description)
        .append("\n")
        .append(feed)
        .append("\n")
        .append(imagePath);
    return sb.toString();
  }


  public static String hash(Podcast podcast) {
    HashCode hash = Hashing.crc32().newHasher()
        .putString(podcast.title, Charsets.UTF_8)
        .putString(podcast.description, Charsets.UTF_8)
        .hash();
    return hash.toString();
  }
}
