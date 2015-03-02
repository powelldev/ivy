package com.fireminder.podcastcatcher.utils;

import android.net.ParseException;

import com.fireminder.podcastcatcher.models.Episode;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RssParseHelper {

  public static final String LOG_TAG = RssParseHelper.class.getSimpleName();

  public static XmlPullParser setupParser(InputStream stream) throws XmlPullParserException {
    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
    factory.setNamespaceAware(true);
    XmlPullParser xpp = factory.newPullParser();
    xpp.setInput(stream, null);
    return xpp;
  }


    /*
    public static List<Episode> parseEpisodesFromXml(InputStream stream, String podcastId)
            throws XmlPullParserException, IOException, ParseException, java.text.ParseException {
        List<Episode> episodes = new ArrayList<Episode>();
        final int ITEM_DEPTH = 4;
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(stream, Constants.UTF_8);
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
                    Logger.d(LOG_TAG, "XML PARSER: start: " + name);
                    if (name.matches("item")) {
                        Logger.d(LOG_TAG, "XML PARSER: in item");
                        test = "item started" + "\n";
                    }
                    if (name.matches("title") && xpp.getDepth() == ITEM_DEPTH) {
                        content = xpp.nextText();
                        test += content + "\n";
                        episode.title = content;
                        episode.podcastId = podcastId;
                    } else if (name.matches("description")
                            && xpp.getDepth() == ITEM_DEPTH) {
                        content = xpp.nextText();
                        test += content + "\n";
                        content = android.text.Html.fromHtml(content).toString();
                        episode.description = content;
                    } else if (name.matches("pubDate")
                            && xpp.getDepth() == ITEM_DEPTH) {
                        content = xpp.nextText();
                        test += content + "\n";
                        Date pubDate = pubDateFormatter.parse(content);
                        Logger.d(LOG_TAG, "pubdate" + content);
                        episode.pubDate = pubDate.getTime();
                    } else if (name.matches("enclosure")
                            && xpp.getDepth() == ITEM_DEPTH) {
                        content = xpp.getAttributeValue(null, "url");
                        encl = true;
                        test += content + "\n";
                        episode.streamUri = content;
                    }
                    break;

                case (XmlPullParser.END_TAG):
                    Logger.d(LOG_TAG, "XML PARSER:" + "end: " + name);
                    if (name.matches("item")) {
                        Logger.d(LOG_TAG, "XML PARSER: " + "out item");
                        test += "item ended" + "\n";
                        if (encl == true) {
                            testStringList.add(test);
                            episode.elapsed = 0;
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
            Logger.d(LOG_TAG, "TestString" + s);
        }
        Logger.d(LOG_TAG, "ParseEpisodeTiming" + ""
                + ((System.nanoTime() - startTime) / 1000));
        return episodes;
    } // end parseEpisodesFromXml()
    */

  public static List<Episode> parseNewEpisodesFromXml(InputStream stream,
                                                      String podcastId, long oldPubDate) throws XmlPullParserException,
      IOException, ParseException, java.text.ParseException {
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
        "EEE, dd MMM yyy HH:mm:ss zzzz", Locale.US);

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
            episode.title = content;
          } else if (name.matches("description")
              && xpp.getDepth() == ITEM_DEPTH) {
            content = xpp.nextText();
            test += content + "\n";
            content = android.text.Html.fromHtml(content).toString();
            episode.description = content;
          } else if (name.matches("pubDate")
              && xpp.getDepth() == ITEM_DEPTH) {
            content = xpp.nextText();
            test += content + "\n";
            Date pubDate = pubDateFormatter.parse(content);
            if (oldPubDate == pubDate.getTime()) {
              Logger.d(LOG_TAG, "Old episode found");
              oldEpisodeFound = true;
              break;
            }
            episode.pubDate = pubDate.getTime();
          } else if (name.matches("enclosure")
              && xpp.getDepth() == ITEM_DEPTH) {
            content = xpp.getAttributeValue(null, "url");
            encl = true;
            test += content + "\n";
            episode.streamUri = content;
          }
          break;
        case (XmlPullParser.END_TAG):
          if (name.matches("item")) {
            test += "item ended" + "\n";
            if (encl == true && oldEpisodeFound == false) {
              testStringList.add(test);
              episode.podcastId = podcastId;
              episode.elapsed = 0;
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
      Logger.d(LOG_TAG, "TestString" + s);
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
            Logger.d(LOG_TAG, "Beginning of OPML");
            break;
          case XmlPullParser.START_TAG:
            if (xpp.getName().equals("opml")) {
              isInOpml = true;
              Logger.d(LOG_TAG, "Beginning of OPML tree");
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
      Logger.e(LOG_TAG, "Err opmlasync: " + e.getMessage());
      return null;
    }
  }
}
