package com.fireminder.podcastcatcher.models;

import android.database.Cursor;

import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;
import com.fireminder.podcastcatcher.utils.Utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class PodcastHandler {
    private static final String LOG_TAG = PodcastHandler.class.getSimpleName();
    private static final int NUM_PODCAST_ITEMS = 3;
    private static final int XML_CHANNEL_DEPTH = 3;

    private List<Podcast> mPodcasts;

    public static Podcast parsePodcastFromCursor(Cursor cursor) {
        Podcast podcast = new Podcast();
        podcast.title = cursor.getString(cursor.getColumnIndex(PodcastCatcherContract.Podcasts.PODCAST_TITLE));
        podcast.podcastId = cursor.getString(cursor.getColumnIndex(PodcastCatcherContract.Podcasts.PODCAST_ID));
        podcast.imagePath = cursor.getString(cursor.getColumnIndex(PodcastCatcherContract.Podcasts.PODCAST_IMAGEURL));
        podcast.description = cursor.getString(cursor.getColumnIndex(PodcastCatcherContract.Podcasts.PODCAST_DESCRIPTION));
        podcast.feed = cursor.getString(cursor.getColumnIndex(PodcastCatcherContract.Podcasts.PODCAST_FEED));
        return podcast;
    }

    public static Podcast parsePodcast(String response) throws XmlPullParserException, IOException {
        // Some feeds can contain a Byte Order mark before xml begins
        // this regex removes those before parsing.
        response = response.replaceAll("^.*<", "<");
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(new StringReader(response));
        Podcast podcast = new Podcast();
        int eventType = parser.getEventType();
        int itemsCounter = 0;

        while (eventType != XmlPullParser.END_DOCUMENT
                && itemsCounter < NUM_PODCAST_ITEMS) {
            switch (eventType) {
                case (XmlPullParser.START_TAG):
                    if (parser.getDepth() == XML_CHANNEL_DEPTH) {
                        String podcastItem = parser.getName();
                        if (podcastItem.matches("title")) {
                            podcastItem = parser.nextText();
                            podcast.title = podcastItem;
                            podcast.podcastId = Utils.computeHash(podcastItem);
                            itemsCounter++;
                        } else if (podcastItem.matches("description")) {
                            podcastItem = parser.nextText();
                            podcastItem = android.text.Html.fromHtml(podcastItem)
                                    .toString();
                            podcast.description = podcastItem;
                            itemsCounter++;
                        } else if (podcastItem.matches("image")) {
                            if (parser.getPrefix() != null
                                    && parser.getPrefix().matches("itunes")) {
                                String imagelink = parser.getAttributeValue(0);
                                podcast.imagePath = imagelink;
                                itemsCounter++;
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
            eventType = parser.next();
        }
        return podcast;
    }
}
