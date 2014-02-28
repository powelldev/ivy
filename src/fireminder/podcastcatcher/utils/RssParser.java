package fireminder.podcastcatcher.utils;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fireminder.podcastcatcher.ui.LazyAdapter;
import fireminder.podcastcatcher.valueobjects.Episode;
import fireminder.podcastcatcher.valueobjects.Podcast;

/***
 * Contains methods for parsing data from RSS feeds.
 */
public class RssParser {

    public static final String TAG = RssParser.class.getSimpleName();
    static final String TITLE = "title";
    static final String DESCRIP = "description";
    static final String LINK = "link";
    static final String IMAGELINK = "imagelink";

    public RssParser() {

    }

    public static Podcast parsePodcastFromXml(InputStream stream)
            throws XmlPullParserException, IOException {
        Podcast podcast = new Podcast();
        final int NUM_PODCAST_ITEMS = 3;
        final int XML_CHANNEL_DEPTH = 3;
        int itemsCounter = 0;
        String podcastItem = "";
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);

        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(stream, null); // BUG SQUASHED

        int eventType = xpp.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT
                && itemsCounter < NUM_PODCAST_ITEMS) {
            switch (eventType) {
            case (XmlPullParser.START_TAG):
                if (xpp.getDepth() == XML_CHANNEL_DEPTH) {
                    podcastItem = xpp.getName();
                    if (podcastItem.matches("title")) {
                        podcastItem = xpp.nextText();
                        Log.d("TITLE: ", podcastItem);
                        podcast.setTitle(podcastItem);
                        itemsCounter++;
                    } else if (podcastItem.matches("description")) {
                        podcastItem = xpp.nextText();
                        Log.d("DESCRIP: ", podcastItem);
                        podcastItem = android.text.Html.fromHtml(podcastItem)
                                .toString();
                        podcast.setDescription(podcastItem);
                        itemsCounter++;
                    } else if (podcastItem.matches("image")) {
                        if (xpp.getPrefix() != null
                                && xpp.getPrefix().matches("itunes")) {
                            String imagelink = xpp.getAttributeValue(0);
                            podcast.setImagePath(imagelink);
                            itemsCounter++;
                        }
                    }
                }
                break;
            default:
                break;
            }
            eventType = xpp.next();
        }
        return podcast;

    }

    public static List<Episode> parseEpisodesFromXml(InputStream stream, long id)
            throws XmlPullParserException, IOException, ParseException {
        List<Episode> episodes = new ArrayList<Episode>();
        final int ITEM_DEPTH = 4;
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(stream, null);
        int eventType = xpp.getEventType();
        String name = "";
        String test = "";
        String content = "";
        SimpleDateFormat pubDateFormatter = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss zzzz");
        Episode episode = new Episode();
        List<String> testStringList = new ArrayList<String>();
        boolean encl = false;
        long startTime = System.nanoTime();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            name = xpp.getName();
            switch (eventType) {
            case (XmlPullParser.START_TAG):
                // Log.d("XML PARSER:", "start: " + name);
                if (name.matches("item")) {
                    // Log.d("XML PARSER: ", "in item");
                    test = "item started" + "\n";
                }
                if (name.matches("title") && xpp.getDepth() == ITEM_DEPTH) {
                    content = xpp.nextText();
                    test += content + "\n";
                    episode.setTitle(content);
                } else if (name.matches("description")
                        && xpp.getDepth() == ITEM_DEPTH) {
                    content = xpp.nextText();
                    test += content + "\n";
                    content = android.text.Html.fromHtml(content).toString();
                    episode.setDescription(content);
                } else if (name.matches("pubDate")
                        && xpp.getDepth() == ITEM_DEPTH) {
                    content = xpp.nextText();
                    test += content + "\n";
                    Date pubDate = pubDateFormatter.parse(content);
                    Log.d("pubdate", content);
                    Log.d("pubDate",
                            ""
                                    + LazyAdapter.getDate(pubDate.getTime(),
                                            "EEE, dd MMM yyyy HH:mm:ss zzzz"));
                    episode.setPubDate(pubDate.getTime());
                } else if (name.contains("duration")
                        && xpp.getDepth() == ITEM_DEPTH) {
                    content = xpp.nextText();
                    encl = true;
                    test += content + "\n";
                    episode.setDuration(content);
                } else if (name.matches("enclosure")
                        && xpp.getDepth() == ITEM_DEPTH) {
                    content = xpp.getAttributeValue(null, "url");
                    encl = true;
                    test += content + "\n";
                    episode.setUrl(content);
                }
                break;

            case (XmlPullParser.END_TAG):
                // Log.d("XML PARSER:", "end: " + name);
                if (name.matches("item")) {
                    // Log.d("XML PARSER: ", "out item");
                    test += "item ended" + "\n";
                    if (encl == true) {
                        testStringList.add(test);
                        episode.setPodcast_id(id);
                        episode.setElapsed(0);
                        episode.setPlaylistRank(-1);
                        episodes.add(episode);
                        episode = new Episode();
                        test = "";
                        encl = false;
                    } else {
                        test = "";
                        encl = false;
                    }
                }
                break;
            }

            eventType = xpp.next();
        } // end while
        for (String s : testStringList) {
            Log.d("TestString", s);
        }
        Log.e("ParseEpisodeTiming", ""
                + ((System.nanoTime() - startTime) / 1000));
        return episodes;
    } // end parseEpisodesFromXml()

    public static List<Episode> parseNewEpisodesFromXml(InputStream stream,
            long id, long oldPubDate) throws XmlPullParserException,
            IOException, ParseException {
        List<Episode> episodes = new ArrayList<Episode>();
        final int ITEM_DEPTH = 4;
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(stream, null);

        int eventType = xpp.getEventType();

        String name = "";
        String test = "";
        String content = "";

        SimpleDateFormat pubDateFormatter = new SimpleDateFormat(
                "EEE, dd MMM yyy HH:mm:ss zzzz");

        Episode episode = new Episode();
        List<String> testStringList = new ArrayList<String>();

        boolean encl = false;
        boolean oldEpisodeFound = false;

        while (eventType != XmlPullParser.END_DOCUMENT || !oldEpisodeFound) {
            name = xpp.getName();
            switch (eventType) {
            case (XmlPullParser.START_TAG):
                if (name.matches("item")) {
                    test = "item started" + "\n";
                }
                if (name.matches("title") && xpp.getDepth() == ITEM_DEPTH) {
                    content = xpp.nextText();
                    test += content + "\n";
                    episode.setTitle(content);
                } else if (name.matches("description")
                        && xpp.getDepth() == ITEM_DEPTH) {
                    content = xpp.nextText();
                    test += content + "\n";
                    content = android.text.Html.fromHtml(content).toString();
                    episode.setDescription(content);
                } else if (name.matches("pubDate")
                        && xpp.getDepth() == ITEM_DEPTH) {
                    content = xpp.nextText();
                    test += content + "\n";
                    Date pubDate = pubDateFormatter.parse(content);
                    if (oldPubDate == pubDate.getTime()) {
                        Log.d(TAG, "Old episode found");
                        oldEpisodeFound = true;
                        break;
                    }
                    episode.setPubDate(pubDate.getTime());
                } else if (name.contains("duration")
                        && xpp.getDepth() == ITEM_DEPTH) {
                    content = xpp.nextText();
                    encl = true;
                    test += content + "\n";
                    episode.setDuration(content);
                } else if (name.matches("enclosure")
                        && xpp.getDepth() == ITEM_DEPTH) {
                    content = xpp.getAttributeValue(null, "url");
                    encl = true;
                    test += content + "\n";
                    episode.setUrl(content);
                }
                break;
            case (XmlPullParser.END_TAG):
                if (name.matches("item")) {
                    test += "item ended" + "\n";
                    if (encl == true && oldEpisodeFound == false) {
                        testStringList.add(test);
                        episode.setPodcast_id(id);
                        episode.setElapsed(0);
                        episode.setPlaylistRank(-1);
                        episodes.add(episode);
                        episode = new Episode();
                        test = "";
                        encl = false;
                    } else {
                        test = "";
                        encl = false;
                    }
                }
                break;
            }
            eventType = xpp.next();
        } // end while
        for (String s : testStringList) {
            Log.d("TestString", s);
        }
        return episodes;
    } // end parseNew

    public static List<String> parseOpmlForPodcasts(BufferedReader reader) {
        try {
            List<String> podcasts = new ArrayList<String>();
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(reader);
            int eventType = xpp.getEventType();
            boolean isInOpml = false;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    Log.d(TAG, "Beginning of OPML");
                    break;
                case XmlPullParser.START_TAG:
                    if (xpp.getName().equals("opml")) {
                        isInOpml = true;
                        Log.d(TAG, "Beginning of OPML tree");
                    } else if (isInOpml && xpp.getName().equals("outline")) {
                        String url = "";
                        url = xpp.getAttributeValue(null, "xmlUrl");
                        if (url != "") {
                            podcasts.add(url);
                        }
                    }
                    break;
                }
                eventType = xpp.next();
            }
            return podcasts;
        } catch (Exception e) {
            Log.e(Utils.TAG, "Err opmlasync: " + e.getMessage());
            return null;
        }
    }
}
