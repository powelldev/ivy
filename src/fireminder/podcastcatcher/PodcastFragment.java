package fireminder.podcastcatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.xmlpull.v1.XmlPullParserException;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
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
import android.widget.ListView;
import android.widget.Toast;
import fireminder.podcastcatcher.activities.ChannelActivity;
import fireminder.podcastcatcher.db.EpisodeDAO;
import fireminder.podcastcatcher.db.Podcast;
import fireminder.podcastcatcher.db.PodcastDAO;
import fireminder.podcastcatcher.db.PodcastSqlHelper;
import fireminder.podcastcatcher.ui.PodcastAdapter;

@SuppressWarnings("deprecation")
public class PodcastFragment extends ListFragment{

	final static String TAG = PodcastFragment.class.getSimpleName();
	
	/* Podcast Database Object */
	public PodcastDAO podcastDao;
	public BackgroundThread bt = new BackgroundThread(getActivity());
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initialize();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.listfragment1, container, false);
		
		podcastDao = new PodcastDAO(getActivity());
		podcastDao.open();
		
		updateListAdapter(getActivity());
		
		//ImageButton addPodcastBtn = (ImageButton) rootView.findViewById(R.id.subscribe_btn);
		//addPodcastBtn.setOnClickListener(subscribeClickListener);
        
		/*Button refreshBtn = (Button) rootView.findViewById(R.id.refresh_btn);
		refreshBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
					BackgroundThread bt = new BackgroundThread(getActivity());
						bt.getNewEpisodes();
			}
			
		});*/
		
		
        subscribeIfIntent();

		return rootView;
	}

    private void subscribeIfIntent(){
        try {
            Bundle b = this.getArguments();
            subscribe( b.getString("uri") );
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public void subscribe(String data){
		//Create inflater for view for the Alert Dialog
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		View promptsView = inflater.inflate(R.layout.subscribe_dialog, null);
		final EditText userInput = (EditText) promptsView.findViewById(R.id.rss_feed);
		final ImageButton paste_btn = (ImageButton) promptsView.findViewById(R.id.paste_btn);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(promptsView);		
		//Set listener for the paste button
		userInput.setText(data);
		userInput.setSelection(data.length());
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
				//BackgroundThread bt = new BackgroundThread(getActivity());
				//bt.getPodcastInfoFromBackgroundThread(userString);
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
	// Subscription dialog presenter. Will prompt user for a URL and call the validator.
	private OnClickListener subscribeClickListener = new OnClickListener(){
		@Override
		public void onClick(View button) {
			//Create inflater for view for the Alert Dialog
			LayoutInflater inflater = getActivity().getLayoutInflater();
			
			View promptsView = inflater.inflate(R.layout.subscribe_dialog, null);
			final EditText userInput = (EditText) promptsView.findViewById(R.id.rss_feed);
			userInput.setSelection(7);
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
					//BackgroundThread bt = new BackgroundThread(getActivity());
					//bt.getPodcastInfoFromBackgroundThread(userString);
				    if(BackgroundThread.isHTTPAvailable()) {	
					    new HttpDownloadTask().execute(userString);
                    }
                    else {
                        Toast.makeText(getActivity(), "Connection Unavaliable", Toast.LENGTH_LONG).show();
                    }
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
		//SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1,
		//		podcastCursor, new String[] { PodcastSqlHelper.COLUMN_TITLE }, new int[] { android.R.id.text1 }, 2);
		PodcastAdapter cursorAdapter = new PodcastAdapter(getActivity(), podcastCursor, 0);
		setListAdapter(cursorAdapter);
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
				//podcastData = RssParser.parsePodcastFromXml(reader);
				podcastData = RssParser.parsePodcastFromXml(is);
				// Add podcast url to content value
				podcastData.put(PodcastSqlHelper.COLUMN_LINK, urls[0]);
			} 
			catch(MalformedURLException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (XmlPullParserException e) {
				e.printStackTrace();
				return null;
			}
			return podcastData;
		}
		
		@Override
		protected void onPostExecute(ContentValues result){
			podcastDao.open();

			// Delete the placeholder "Loading ..." item
			podcastDao.deletePodcast(id);			
			if(result != null){
				Podcast podcast = podcastDao.insertPodcast(result);
				BackgroundThread bt = new BackgroundThread(getActivity());
				bt.getEpisodesFromBackgroundThread(podcast.getLink(), podcast.get_id());
//				bt.getPodcastImageFromBackgroundThread(podcast.getLink(), podcast.get_id());
				Log.d(TAG, "parsing for episodes");
				//new ParseXmlForEpisodes().execute(new String[] {podcast.getLink(), String.valueOf(podcast.get_id())});
			}
			else{
				Toast.makeText(getActivity(), "Podcast subscription failed: Please check url", Toast.LENGTH_LONG).show();
			}
			updateListAdapter(getActivity());
			
		}

	}
	
	OnItemClickListener channelListViewOnClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int itemPosition,
				long itemId) {
//			Helper.getNewEpisodesFromPodcast(getActivity() , itemId);

			Intent intent = new Intent();
			intent.setClass(getActivity(), ChannelActivity.class);
			intent.putExtra("channel_id", itemId);
			
			startActivity(intent);
			
		}
	};
	OnItemLongClickListener channelListViewOnItemLongClickListener = 
			new OnItemLongClickListener(){
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int itemPosition, long itemId) {
			podcastDao.deletePodcast(itemId);
			// TODO delete items from playlist
			EpisodeDAO edao = new EpisodeDAO(getActivity());
			edao.open();
			edao.deleteAllEpisodes(itemId);
			edao.close();
			updateListAdapter(getActivity());
			return false;
		}
	};
	
	private void initialize() {
		ListView listView = getListView();
		listView.setOnItemClickListener(channelListViewOnClickListener);
		listView.setOnItemLongClickListener(channelListViewOnItemLongClickListener);
	}


	
}
