package fireminder.podcastcatcher;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.http.util.ByteArrayBuffer;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import fireminder.podcastcatcher.db.EpisodeDAO;
import fireminder.podcastcatcher.db.EpisodeSqlHelper;
import fireminder.podcastcatcher.db.Podcast;
import fireminder.podcastcatcher.db.PodcastDAO;
import fireminder.podcastcatcher.db.PodcastSqlHelper;

@SuppressWarnings("deprecation")
public class PodcastFragment extends ListFragment {

	public PodcastDAO podcastDao = null;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Item Click - launch channel view via activity
		getListView().setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int itemPosition,
					long itemId) {
				Intent intent = new Intent();
				intent.setClass(getActivity(), ChannelActivity.class);
				intent.putExtra("channel_id", itemId);
				startActivity(intent);
				
			}
		});
		// LongItemPress - deletes podcast from db
		getListView().setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int itemPosition, long itemId) {
				podcastDao.deletePodcast(itemId);
				EpisodeDAO edao = new EpisodeDAO(getActivity());
				edao.open();
				edao.deleteAllEpisodes(itemId);
				edao.close();
				updateListAdapter(getActivity());
				return false;
			}
		});
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.listfragment1, container, false);
		podcastDao = new PodcastDAO(getActivity());
		podcastDao.open();
		updateListAdapter(getActivity());
		
		ImageButton addPodcastBtn = (ImageButton) rootView.findViewById(R.id.subscribe_btn);
		addPodcastBtn.setOnClickListener(subscribeClickListener);
		
		
		return rootView;
	}
	// Subscription dialog presenter. Will prompt user for a URL and call the validator.
	private OnClickListener subscribeClickListener = new OnClickListener(){
		@Override
		public void onClick(View button) {
			//Create inflater for view for the Alert Dialog
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View promptsView = inflater.inflate(R.layout.subscribe_dialog, null);
			final EditText userInput = (EditText) promptsView.findViewById(R.id.rss_feed);
			final ImageButton paste_btn = (ImageButton) promptsView.findViewById(R.id.paste_btn);
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setView(promptsView);		
			//Set listener for the paste button
			paste_btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
					String pasteData = null;
					if(clipboard.hasText()){
						pasteData = clipboard.getText().toString();
					}
					if(pasteData!=null){
						userInput.setText(pasteData);
					}
				}
			});
			//Get user URL, parse it, update ListView, update database
			builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String userString = userInput.getText().toString();
					new HttpDownloadTask().execute(userString);
				}
			});
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			
			builder.create().show();
		}
	};
	
	public void updateListAdapter(Context context){
		podcastDao.open();
		Cursor podcastCursor = podcastDao.getAllPodcastsAsCursor();
		SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1,
				podcastCursor, new String[] { PodcastSqlHelper.COLUMN_TITLE }, new int[] { android.R.id.text1 }, 2);
		setListAdapter(cursorAdapter);
	}
	

	private class ParseXmlForImage extends AsyncTask<String, Void, ByteArrayBuffer>{
		String idForQuery;
		
		@Override
		protected ByteArrayBuffer doInBackground(String... urls) {
			URL url;
			idForQuery = urls[1];
			BufferedReader reader = null;
			ByteArrayBuffer baf = null;
			String imagelink = null;
			try {
				url = new URL(urls[0]);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				InputStream is = con.getInputStream();
				reader = new BufferedReader( new InputStreamReader(is));
				imagelink = RSSReader.parsePodcastImageFromXml(reader);
				
				//Download image
				URL imageurl = new URL(imagelink);
				HttpURLConnection conn = (HttpURLConnection) imageurl.openConnection();
				InputStream istream = conn.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(istream, 128);
				baf = new ByteArrayBuffer(128);
				int current = 0;
				while ((current = bis.read()) != -1){
					baf.append((byte) current);
				}
				} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(Exception e){
				e.printStackTrace();
			}
			
			return baf;
		}
		
		@Override
		protected void onPostExecute(ByteArrayBuffer result){
			if(result != null){
				Log.d("Add this to db: ", result + " AT " + idForQuery);
				Podcast podcast = podcastDao.getPodcast(Long.parseLong(idForQuery));
				podcast.setImagelink(result.toByteArray());
				podcastDao.updatePodcastImagelink(podcast);
				new ParseXmlForEpisodes().execute(new String[] {podcast.getLink(), String.valueOf(podcast.get_id())});
			} else {
				Podcast podcast = podcastDao.getPodcast(Long.parseLong(idForQuery));
				Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
				byte[] byteArray = stream.toByteArray();
				podcast.setImagelink(byteArray);
				podcastDao.updatePodcastImagelink(podcast);
			}

			
		}
		
	}
	
	private class ParseXmlForEpisodes extends AsyncTask<String, Void, ContentValues>{
		long idForQuery;
		EpisodeDAO edao;
		
		@Override
		protected void onPreExecute(){
		}
		@Override
		protected ContentValues doInBackground(String... urls) {
			URL url;
			idForQuery = Long.parseLong(urls[1]);
			BufferedReader reader = null;
			List<ContentValues> episodes = null;
			try {

				url = new URL(urls[0]);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				InputStream is = con.getInputStream();
				reader = new BufferedReader( new InputStreamReader(is));
				Log.d("EpisodeParsing", "here: " + urls[0] + " " + urls[1]);
				episodes = RSSReader.parseEpisodesFromXml(reader, idForQuery);
				edao = new EpisodeDAO(getActivity());
				edao.open();
				for(ContentValues e : episodes){
					Log.d("Inserting: ", e.getAsString(EpisodeSqlHelper.COLUMN_TITLE));
					edao.insertEpisode(e);
				}
				edao.close();
				
				return null;
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		
	}
		@Override
		protected void onPostExecute(ContentValues result) {
			
		}
	}
	private class HttpDownloadTask extends AsyncTask<String, Void, ContentValues>{
		long id;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			podcastDao.open();
			id = podcastDao.createAndInsertPodcast("Loading ...").get_id();
			updateListAdapter(getActivity());
		}

		@Override
		protected ContentValues doInBackground(String... urls) {
			
			BufferedReader reader = null;
			ContentValues podcastData = null;
			try{
				URL url = new URL(urls[0]);
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				InputStream is = con.getInputStream();
				reader = new BufferedReader(new InputStreamReader(is));
				//Pass reader to parser
				podcastData = RSSReader.parsePodcastFromXml(reader);
				// Add podcast url to content value
				podcastData.put(PodcastSqlHelper.COLUMN_LINK, urls[0]);
			} 
			catch(Exception e) {
					e.printStackTrace();
				return null;
			}
			return podcastData;
		}
		
		@Override
		protected void onPostExecute(ContentValues result){
			podcastDao.open();
			// Delete "Loading ..." item
			podcastDao.deletePodcast(id);			
			if(result != null){
				Podcast podcast = podcastDao.insertPodcast(result);
				new ParseXmlForImage().execute(new String[] {podcast.getLink(), String.valueOf(podcast.get_id())});
				Log.d("starting", "parsing for episodes");
				//new ParseXmlForEpisodes().execute(new String[] {podcast.getLink(), String.valueOf(podcast.get_id())});
			}
			else{
				Toast.makeText(getActivity(), "Podcast subscription failed =(, please check url", Toast.LENGTH_LONG).show();
			}
			updateListAdapter(getActivity());
			
		}

	}

/*
 * 	public List<String> parsePodcastInfoFromXml(BufferedReader reader) throws IOException, XmlPullParserException {
		List<String> podcastData = new ArrayList<String>();
		final int NUM_PODCAST_ITEMS = 4;
		for(int i = 0; i < 4; i++){
			podcastData.add(i, "");
		}
		
		 // continueParsingFlag - stops parsing if enough data items have been found to identify podcast
		 
		boolean continueParsingFlag = true;
		int 	dataItemsCounter = 0;
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		xpp.setInput(reader);
		int eventType = xpp.getEventType();
		while(eventType != XmlPullParser.END_DOCUMENT && (continueParsingFlag == true)){
			switch(eventType){
			case(XmlPullParser.START_DOCUMENT):
				break;
			case(XmlPullParser.START_TAG):
				//DEPTH 3 for <channel>
				if(xpp.getDepth() == 3){
					if(xpp.getName().matches("title")){
						String item = xpp.nextText();
						Log.d("TITLE: ", item);
						podcastData.add(0, item);
						dataItemsCounter++;
					}
					else if(xpp.getName().matches("link")){
						String item = xpp.nextText();
						Log.d("LINK: ", item);
						podcastData.add(2, item);
						dataItemsCounter++;
					}
					else if(xpp.getName().matches("description")){
						String item = xpp.nextText();
						Log.d("Description: ", item);
						podcastData.add(1, item);
						dataItemsCounter++;
					}
				}
				if(dataItemsCounter >= 4) { continueParsingFlag = false; }
			
				break;
			case(XmlPullParser.END_TAG):
				break;
			default:
				break;
			}
			eventType = xpp.next();
		}
		for(String s : podcastData){
			Log.d("parse result:", s);			
		}
		return podcastData;
	}
 */
}
