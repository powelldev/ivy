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
  public String episodeId;
  public String description;
  public String streamUri;
  public String localUri;
  public long pubDate;
  public long duration;
  public long elapsed;
  public String podcastId;
  public DownloadStatus downloadStatus;
  public boolean isComplete;

  public enum DownloadStatus {
    DOWNLOADED(0),
    NOT_DOWNLOADED(1),
    DOWNLOAD_IN_PROGRESS(2),
    DOWNLOAD_INTERRUPTED(3);

    public final int id;
    DownloadStatus(int id) {
      this.id = id;
    }

    public static DownloadStatus from(int id) {
      for (DownloadStatus ds : DownloadStatus.values()) {
        if (ds.id == id) {
          return ds;
        }
      }
      throw new IllegalArgumentException("No download status for id: " + id);
    }
  }

  public Episode(Parcel source) {
    title = source.readString();
    episodeId = source.readString();
    description = source.readString();
    streamUri = source.readString();
    localUri = source.readString();
    pubDate = source.readLong();
    duration = source.readLong();
    elapsed = source.readLong();
    podcastId = source.readString();
    downloadStatus = DownloadStatus.from(source.readInt());
    isComplete = source.readInt() == 1;
  }

  public Episode() {}

  /* Generate unique Episode id */
  public static String hash(Episode episode) {
    HashCode hash = Hashing.crc32().newHasher()
        .putString(episode.title, Charsets.UTF_8)
        .putString(episode.streamUri, Charsets.UTF_8)
        .putLong(episode.pubDate)
        .hash();
    return hash.toString();
  }

  /**
   * Note that if the cursor is newly created please call next() or moveToFirst() prior to calling this.
   *
   * @param cursor
   * @return
   */
  public static Episode parseEpisodeFromCursor(Cursor cursor) {
    Episode episode = new Episode();
    episode.title = cursor.getString(cursor.getColumnIndex(PodcastCatcherContract.Episodes.EPISODE_TITLE));
    episode.episodeId = cursor.getString(cursor.getColumnIndex(PodcastCatcherContract.Episodes.EPISODE_ID));
    episode.description = cursor.getString(cursor.getColumnIndex(PodcastCatcherContract.Episodes.EPISODE_DESCRIPTION));
    episode.streamUri = cursor.getString(cursor.getColumnIndex(PodcastCatcherContract.Episodes.EPISODE_STREAM_URI));
    episode.localUri = cursor.getString(cursor.getColumnIndex(PodcastCatcherContract.Episodes.EPISODE_LOCAL_URI));
    episode.pubDate = cursor.getLong(cursor.getColumnIndex(PodcastCatcherContract.Episodes.EPISODE_PUBLICATION_DATE));
    episode.duration = cursor.getLong(cursor.getColumnIndex(PodcastCatcherContract.Episodes.EPISODE_CONTENT_DURATION));
    episode.elapsed = cursor.getLong(cursor.getColumnIndex(PodcastCatcherContract.Episodes.EPISODE_PERCENT_ELAPSED));
    episode.podcastId = cursor.getString(cursor.getColumnIndex(PodcastCatcherContract.Podcasts.PODCAST_ID));
    episode.downloadStatus = DownloadStatus.from(cursor.getInt(cursor.getColumnIndex(PodcastCatcherContract.Episodes.EPISODE_DOWNLOAD_STATUS)));
    episode.isComplete = cursor.getInt(cursor.getColumnIndex(PodcastCatcherContract.Episodes.EPISODE_IS_COMPLETE)) == 1;
    return episode;
  }

  public static List<Episode> parseEpisodesFromResponse(String response) throws DocumentException, java.text.ParseException {
    response = Utils.validateXml(response);

    Element channel = DocumentHelper.parseText(response).getRootElement().element("channel");
    List<Episode> episodes = new ArrayList<>(channel.elements().size());
    SimpleDateFormat pubDateFormatter = new SimpleDateFormat(
        "EEE, dd MMM yyyy HH:mm:ss zzzz", Locale.US);
    for (Element item : channel.elements("item")) {
      Episode episode = new Episode();

      episode.title = item.elementText("title");
      episode.description = item.elementText("description");
      if (episode.description == null) {
        episode.description = episode.title;
      }
      episode.pubDate = pubDateFormatter.parse(item.elementText("pubDate").trim()).getTime();
      try {
        episode.streamUri = item.element("enclosure").attributeValue("url");
      } catch (NullPointerException e) {
        // Item lacks media, skip
        continue;
      }

            /* This value is very often wrong. Delaying implementation. */
      //episode.media_length = item.element("enclosure").attributeValue("length");

      episode.episodeId = hash(episode);
      episode.duration = 0;
      episode.elapsed = 0;
      episode.localUri = "";
      episode.downloadStatus = DownloadStatus.NOT_DOWNLOADED;
      episode.isComplete = false;

      episodes.add(episode);
    }
    return episodes;
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

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(title);
    dest.writeString(episodeId);
    dest.writeString(description);
    dest.writeString(streamUri);
    dest.writeString(localUri);
    dest.writeLong(pubDate);
    dest.writeLong(duration);
    dest.writeLong(elapsed);
    dest.writeString(podcastId);
    dest.writeInt(downloadStatus.id);
    dest.writeInt(isComplete ? 1 : 0);
  }

  public static ContentValues episodeToContentValues(Episode episode) {
    ContentValues cv = new ContentValues();
    cv.put(PodcastCatcherContract.Episodes.EPISODE_ID, episode.episodeId);
    cv.put(PodcastCatcherContract.Podcasts.PODCAST_ID, episode.podcastId);
    cv.put(PodcastCatcherContract.Episodes.EPISODE_TITLE, episode.title);
    cv.put(PodcastCatcherContract.Episodes.EPISODE_DESCRIPTION, episode.description);
    cv.put(PodcastCatcherContract.Episodes.EPISODE_STREAM_URI, episode.streamUri);
    cv.put(PodcastCatcherContract.Episodes.EPISODE_LOCAL_URI, episode.localUri);
    cv.put(PodcastCatcherContract.Episodes.EPISODE_PUBLICATION_DATE, episode.pubDate);
    cv.put(PodcastCatcherContract.Episodes.EPISODE_CONTENT_DURATION, episode.duration);
    cv.put(PodcastCatcherContract.Episodes.EPISODE_PERCENT_ELAPSED, episode.elapsed);
    cv.put(PodcastCatcherContract.Episodes.EPISODE_DOWNLOAD_STATUS, episode.downloadStatus.id);
    cv.put(PodcastCatcherContract.Episodes.EPISODE_IS_COMPLETE, episode.isComplete ? 1 : 0);
    return cv;
  }

  @Override
  public String toString() {
    return "Episode{" +
        "title='" + title + '\'' +
        ", episodeId='" + episodeId + '\'' +
        ", description='" + description + '\'' +
        ", streamUri='" + streamUri + '\'' +
        ", localUri='" + localUri + '\'' +
        ", pubDate=" + pubDate +
        ", duration=" + duration +
        ", elapsed=" + elapsed +
        ", podcastId='" + podcastId + '\'' +
        ", isDownloaded=" + downloadStatus.name() +
        ", isComplete=" + isComplete +
        '}';
  }

  public String getUri() {
    return localUri;
  }
}

