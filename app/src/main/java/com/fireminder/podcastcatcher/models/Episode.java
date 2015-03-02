package com.fireminder.podcastcatcher.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;
import com.fireminder.podcastcatcher.utils.Utils;
import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Episode implements Parcelable {
  public String title;
  public String episode_id;
  public String description;
  public String stream_uri;
  public String local_uri;
  public long pubDate;
  public long duration;
  public long elapsed;
  public String podcast_id;
  public boolean isDownloaded;
  public boolean isComplete;

  public Episode(Parcel source) {
    title = source.readString();
    episode_id = source.readString();
    description = source.readString();
    stream_uri = source.readString();
    local_uri = source.readString();
    pubDate = source.readLong();
    duration = source.readLong();
    elapsed = source.readLong();
    podcast_id = source.readString();
    isDownloaded = source.readInt() == 1;
    isComplete = source.readInt() == 1;
  }

  public Episode() {

  }

  public static String hash(Episode episode) {
    HashCode hash = Hashing.crc32().newHasher()
        .putString(episode.title, Charsets.UTF_8)
        .hash();
    return hash.toString();
  }

  public static Creator<Episode> CREATOR = new Creator<Episode>() {
    @Override
    public Episode createFromParcel(Parcel source) {
      return new Episode(source);
    }

    @Override
    public Episode[] newArray(int size) {
      return new Episode[0];
    }
  };

  /**
   * Note that if the cursor is newly created please call next() or moveToFirst() prior to calling this.
   *
   * @param cursor
   * @return
   */
  public static Episode parseEpisodeFromCursor(Cursor cursor) {
    Episode episode = new Episode();
    episode.title = cursor.getString(cursor.getColumnIndex(PodcastCatcherContract.Episodes.EPISODE_TITLE));
    episode.episode_id = cursor.getString(cursor.getColumnIndex(PodcastCatcherContract.Episodes.EPISODE_ID));
    episode.description = cursor.getString(cursor.getColumnIndex(PodcastCatcherContract.Episodes.EPISODE_DESCRIPTION));
    episode.stream_uri = cursor.getString(cursor.getColumnIndex(PodcastCatcherContract.Episodes.EPISODE_STREAM_URI));
    episode.local_uri = cursor.getString(cursor.getColumnIndex(PodcastCatcherContract.Episodes.EPISODE_LOCAL_URI));
    episode.pubDate = cursor.getLong(cursor.getColumnIndex(PodcastCatcherContract.Episodes.EPISODE_PUBLICATION_DATE));
    episode.duration = cursor.getLong(cursor.getColumnIndex(PodcastCatcherContract.Episodes.EPISODE_CONTENT_DURATION));
    episode.elapsed = cursor.getLong(cursor.getColumnIndex(PodcastCatcherContract.Episodes.EPISODE_PERCENT_ELAPSED));
    episode.podcast_id = cursor.getString(cursor.getColumnIndex(PodcastCatcherContract.Podcasts.PODCAST_ID));
    episode.isDownloaded = cursor.getInt(cursor.getColumnIndex(PodcastCatcherContract.Episodes.EPISODE_IS_DOWNLOADED)) == 1;
    episode.isComplete = cursor.getInt(cursor.getColumnIndex(PodcastCatcherContract.Episodes.EPISODE_IS_COMPLETE)) == 1;
    return episode;
  }

  public static List<Episode> parseEpisodesFromResponse(String response) throws DocumentException, java.text.ParseException {
    response = Utils.validateXml(response);

    Element channel = DocumentHelper.parseText(response).getRootElement().element("channel");
    List<Episode> episodes = new ArrayList<>(channel.elements().size());
    SimpleDateFormat pubDateFormatter = new SimpleDateFormat(
        "EEE, dd MMM yyyy HH:mm:ss zzzz", Locale.ENGLISH);
    for (Element item : channel.elements("item")) {
      Episode episode = new Episode();

      episode.title = item.elementText("title");
      episode.description = item.elementText("description");
      if (episode.description == null) {
        episode.description = episode.title;
      }
      episode.pubDate = pubDateFormatter.parse(item.elementText("pubDate").trim()).getTime();
      try {
        episode.stream_uri = item.element("enclosure").attributeValue("url");
      } catch (NullPointerException e) {
        // Item lacks media, skip
        continue;
      }

            /* This value is very often wrong. Delaying implementation. */
      //episode.media_length = item.element("enclosure").attributeValue("length");

      // Items deferred
      episode.episode_id = hash(episode);
      episode.duration = 0;
      episode.elapsed = 0;
      episode.local_uri = "";
      episode.isDownloaded = false;
      episode.isComplete = false;

      episodes.add(episode);
    }
    return episodes;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(title);
    dest.writeString(episode_id);
    dest.writeString(description);
    dest.writeString(stream_uri);
    dest.writeString(local_uri);
    dest.writeLong(pubDate);
    dest.writeLong(duration);
    dest.writeLong(elapsed);
    dest.writeString(podcast_id);
    dest.writeInt(isDownloaded ? 1 : 0);
    dest.writeInt(isComplete ? 1 : 0);
  }

  public static ContentValues episodeToContentValues(Episode episode) {
    ContentValues cv = new ContentValues();
    cv.put(PodcastCatcherContract.Episodes.EPISODE_ID, episode.episode_id);
    cv.put(PodcastCatcherContract.Podcasts.PODCAST_ID, episode.podcast_id);
    cv.put(PodcastCatcherContract.Episodes.EPISODE_TITLE, episode.title);
    cv.put(PodcastCatcherContract.Episodes.EPISODE_DESCRIPTION, episode.description);
    cv.put(PodcastCatcherContract.Episodes.EPISODE_STREAM_URI, episode.stream_uri);
    cv.put(PodcastCatcherContract.Episodes.EPISODE_LOCAL_URI, episode.local_uri);
    cv.put(PodcastCatcherContract.Episodes.EPISODE_PUBLICATION_DATE, episode.pubDate);
    cv.put(PodcastCatcherContract.Episodes.EPISODE_CONTENT_DURATION, episode.duration);
    cv.put(PodcastCatcherContract.Episodes.EPISODE_PERCENT_ELAPSED, episode.elapsed);
    cv.put(PodcastCatcherContract.Episodes.EPISODE_IS_DOWNLOADED, episode.isDownloaded ? 1 : 0);
    cv.put(PodcastCatcherContract.Episodes.EPISODE_IS_COMPLETE, episode.isComplete ? 1 : 0);
    return cv;
  }

  public String getUri() {
    return local_uri;
  }
}

