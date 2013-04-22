package fireminder.podcastcatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import fireminder.podcastcatcher.db.PodcastSQLiteOpenHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

public class RSSReader {
	static final String TITLE = "title";
	static final String DESCRIP = "description";
	static final String LINK = "link";
	static final String IMAGELINK = "imagelink";
	
	
	
	public RSSReader() {
		
	}
	
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
						contentValues.put(PodcastSQLiteOpenHelper.COLUMN_TITLE, podcastItem);
						itemsCounter++;
					}
					else if(podcastItem.matches("description")){
						podcastItem = xpp.nextText();
						Log.d("DESCRIP: ", podcastItem);
						contentValues.put(PodcastSQLiteOpenHelper.COLUMN_DESCRIP, podcastItem);
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
	public List<String> xmlTest(String xml) throws XmlPullParserException, IOException{
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		
		List<String> data = new ArrayList<String>();
		xpp.setInput(new StringReader(xml));
		int eventType = xpp.getEventType();
		while(eventType != XmlPullParser.END_DOCUMENT){
			switch(eventType){
			case(XmlPullParser.START_DOCUMENT):
				Log.d("XML", "Start");
				break;
			case(XmlPullParser.START_TAG):
				Log.d("XML", xpp.getName());
				break;
			case(XmlPullParser.TEXT):
				data.add(xpp.getText());
				Log.d("XML", xpp.getText());
				break;
			case(XmlPullParser.END_TAG):
				Log.d("XML", xpp.getName());
				break;
			case(XmlPullParser.END_DOCUMENT):
				Log.d("XML", "End");
				break;
			default:
				break;
			}
			eventType = xpp.next();
		}
		return data;
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
	
	
	
			
}
