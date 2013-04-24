package fireminder.podcastcatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.ContentValues;
import android.util.Log;
import fireminder.podcastcatcher.db.EpisodeSqlHelper;
import fireminder.podcastcatcher.db.PodcastSqlHelper;
import fireminder.podcastcatcher.ui.LazyAdapter;
/*
 * Contain helper functions for parsing data from RSS feeds.
 */
public class RSSReader {
	static final String TITLE = "title";
	static final String DESCRIP = "description";
	static final String LINK = "link";
	static final String IMAGELINK = "imagelink";
	
	
	
	public RSSReader() {
		
	}
	
	/*
	 * Pulls podcast title and description from RSS feed
	 * Channel Depth is depth of the title and description fields in the xml "tree"
	 * Podcast Items allow parser to exit early if both items have been found
	 */
	public static ContentValues parsePodcastFromXml(BufferedReader reader) throws XmlPullParserException, IOException{
		ContentValues contentValues = new ContentValues();
		final int NUM_PODCAST_ITEMS = 2;
		final int XML_CHANNEL_DEPTH = 3;
		int 	itemsCounter = 0;
		String podcastItem = "";
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		
		XmlPullParser xpp = factory.newPullParser();
		xpp.setInput(reader);
		
		int eventType = xpp.getEventType();
		
		while(eventType != XmlPullParser.END_DOCUMENT && itemsCounter < NUM_PODCAST_ITEMS){
			switch(eventType){
			case(XmlPullParser.START_TAG):
				if(xpp.getDepth() == XML_CHANNEL_DEPTH){
					podcastItem = xpp.getName();
					if(podcastItem.matches("title")){
						podcastItem = xpp.nextText();
						Log.d("TITLE: ", podcastItem);
						contentValues.put(PodcastSqlHelper.COLUMN_TITLE, podcastItem);
						itemsCounter++;
					}
					else if(podcastItem.matches("description")){
						podcastItem = xpp.nextText();
						Log.d("DESCRIP: ", podcastItem);
						contentValues.put(PodcastSqlHelper.COLUMN_DESCRIP, podcastItem);
						itemsCounter++;
					}
				}
				break;
			default:
				break;
			}
			eventType = xpp.next();
		}
		return contentValues;
		
	}

	public static String parsePodcastImageFromXml(BufferedReader reader) throws XmlPullParserException, IOException {
		String imagelink = "";
		boolean imageFound = false;
		final int XML_CHANNEL_DEPTH = 3;
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		
		XmlPullParser xpp = factory.newPullParser();
		xpp.setInput(reader);
		
		int eventType = xpp.getEventType();
		String imageItem = "";
		while(eventType != XmlPullParser.END_DOCUMENT && (!imageFound)){
			switch(eventType){
			case(XmlPullParser.START_TAG):
				if(xpp.getDepth() == XML_CHANNEL_DEPTH){
					imageItem = xpp.getName();
					if(imageItem.matches("image")){
						if(xpp.getPrefix()!=null && xpp.getPrefix().matches("itunes")){
							imagelink = xpp.getAttributeValue(0);
							Log.d("Image linK:", imagelink);
							imageFound = true;
						}
					}
				
			/*		if(imageItem.matches("url")){
						imagelink = xpp.nextText();
						Log.d("Image linK: ", imagelink);
						imageFound = true;
					}
			*/
				}
				break;
			default:
				break;
			}
			eventType = xpp.next();
		}
		return imagelink;
	}

	public static List<ContentValues> parseEpisodesFromXml(BufferedReader reader, long id) throws XmlPullParserException, IOException, ParseException {
		List<ContentValues> episodes = new ArrayList<ContentValues>();
		final int ITEM_DEPTH = 4;
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		xpp.setInput(reader);
		int eventType = xpp.getEventType();
		String name = "";
		String test = "";
		String content = "";
		SimpleDateFormat pubDateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzzz");
		ContentValues cv = new ContentValues();
		List<String> testStringList = new ArrayList<String>();
		boolean encl = false;
		while(eventType != XmlPullParser.END_DOCUMENT){
			name = xpp.getName();
			switch(eventType){
			case(XmlPullParser.START_TAG):
				//Log.d("XML PARSER:", "start: " + name);
				if(name.matches("item")){
					//Log.d("XML PARSER: ", "in item");
					test = "item started" + "\n";
				}
				if(name.matches("title") && xpp.getDepth()==ITEM_DEPTH){
					content = xpp.nextText();
					test += content + "\n";
					cv.put(EpisodeSqlHelper.COLUMN_TITLE, content);
				}
				else if(name.matches("description") && xpp.getDepth()==ITEM_DEPTH){
					content = xpp.nextText();
					test += content + "\n";
					cv.put(EpisodeSqlHelper.COLUMN_DESCRIP, content);
				}
				else if(name.matches("pubDate") && xpp.getDepth()==ITEM_DEPTH){
					content = xpp.nextText();
					test += content + "\n";
					Date pubDate = pubDateFormatter.parse(content);
					Log.d("pubdate", content);
					Log.d("pubDate", "" + LazyAdapter.getDate(pubDate.getTime(), "EEE, dd MMM yyyy HH:mm:ss zzzz"));
					cv.put(EpisodeSqlHelper.COLUMN_PUBDATE, pubDate.getTime());
				}
				else if(name.matches("enclosure") && xpp.getDepth()==ITEM_DEPTH){
					content = xpp.getAttributeValue(null, "url");
					encl = true;
					test += content + "\n";
					cv.put(EpisodeSqlHelper.COLUMN_URL, content);
				}
				break;
			
			case(XmlPullParser.END_TAG):
				//Log.d("XML PARSER:", "end: " + name);
			if(name.matches("item")){
				//Log.d("XML PARSER: ", "out item");
				test += "item ended" + "\n";
				if(encl == true){
					testStringList.add(test);
					cv.put(EpisodeSqlHelper.COLUMN_PODCAST_ID, id);
					episodes.add(cv);
					cv = new ContentValues();
					test = ""; encl = false;
				} else {
					test = ""; encl = false;
				}
			}
				break;
			}
			
			eventType = xpp.next();
		} // end while
		for(String s: testStringList){
			Log.d("TestString", s);
		}
		return episodes;
	} // end parseEpisodesFromXml()


	
	
			
}