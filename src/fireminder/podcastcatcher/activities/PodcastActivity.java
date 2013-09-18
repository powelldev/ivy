package fireminder.podcastcatcher.activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import fireminder.podcastcatcher.BackgroundThread;
import fireminder.podcastcatcher.ADownloadService;
import fireminder.podcastcatcher.PodcastFragment;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.RssParser;
import fireminder.podcastcatcher.db.Podcast;
import fireminder.podcastcatcher.db.PodcastDAO;
import fireminder.podcastcatcher.db.PodcastSqlHelper;
import fireminder.podcastcatcher.ui.PodcastAdapter;

public class PodcastActivity extends ListActivity {

	final static String TAG = PodcastFragment.class.getSimpleName();
	Intent intent;
	static Handler mHandler;

	/* Podcast Database Object */
	public PodcastDAO podcastDao;
	public BackgroundThread bt = new BackgroundThread(this);

	@Override
	protected void onCreate(Bundle icile) {
		super.onCreate(icile);
		setContentView(R.layout.listfragment1);

		podcastDao = new PodcastDAO(this);
		podcastDao.open();
		updateListAdapter(this);
		initialize();
		try {
			String uri = icile.getString("uri");
			subscribe(uri);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Set up download checking
		intent = new Intent(this, ADownloadService.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
				intent, 0);
		AlarmManager alarmManager = (AlarmManager) this
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 0,
				AlarmManager.INTERVAL_DAY, pendingIntent);

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 42 && resultCode == RESULT_OK) {
			// podcastFragment.subscribe(data.getStringExtra("result"));
			Toast.makeText(this, data.getStringExtra("result"),
					Toast.LENGTH_LONG).show();
			subscribe(data.getStringExtra("result"));
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.subscribe_setting:
			subscribe("http://");
			return true;
		case R.id.test:
			this.startService(intent);
		case R.id.get_new:
			BackgroundThread bt = new BackgroundThread(this);
			bt.getNewEpisodes();
			return true;
		case R.id.search:
			Intent i = new Intent(this, SearchActivity.class);
			startActivityForResult(i, 42);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void subscribe(String data) {
		// Create inflater for view for the Alert Dialog
		LayoutInflater inflater = this.getLayoutInflater();

		View promptsView = inflater.inflate(R.layout.subscribe_dialog, null);
		final EditText userInput = (EditText) promptsView
				.findViewById(R.id.rss_feed);
		final ImageButton paste_btn = (ImageButton) promptsView
				.findViewById(R.id.paste_btn);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(promptsView);
		// Set listener for the paste button
		userInput.setText(data);
		userInput.setSelection(data.length());
		paste_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClipboardManager clipboard = (ClipboardManager) getApplicationContext()
						.getSystemService(Context.CLIPBOARD_SERVICE);
				String pasteData = null;
				if (clipboard.hasText()) {
					pasteData = clipboard.getText().toString();
				}
				if (pasteData != null) {
					userInput.setText(pasteData);
				}
			}
		});
		// Get user URL, parse it, update ListView, update database
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String userString = userInput.getText().toString();
				// BackgroundThread bt = new BackgroundThread(getActivity());
				// bt.getPodcastInfoFromBackgroundThread(userString);
				new HttpDownloadTask().execute(userString);
			}
		});

		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		builder.create().show();
	}

	/**
	 * Subscription dialog presenter. Will prompt user for a URL and call the
	 * validator
	 */
	private OnClickListener subscribeClickListener = new OnClickListener() {
		@Override
		public void onClick(View button) {
			// Create inflater for view for the Alert Dialog
			LayoutInflater inflater = ((Activity) getApplicationContext())
					.getLayoutInflater();

			View promptsView = inflater
					.inflate(R.layout.subscribe_dialog, null);
			final EditText userInput = (EditText) promptsView
					.findViewById(R.id.rss_feed);
			userInput.setSelection(7);
			final ImageButton paste_btn = (ImageButton) promptsView
					.findViewById(R.id.paste_btn);

			AlertDialog.Builder builder = new AlertDialog.Builder(
					getApplicationContext());
			builder.setView(promptsView);

			// Set listener for the paste button
			paste_btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ClipboardManager clipboard = (ClipboardManager) getApplicationContext()
							.getSystemService(Context.CLIPBOARD_SERVICE);
					String pasteData = null;
					if (clipboard.hasText()) {
						pasteData = clipboard.getText().toString();
					}
					if (pasteData != null) {
						userInput.setText(pasteData);
					}
				}
			});

			// Get user URL, parse it, update ListView, update database
			builder.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String userString = userInput.getText().toString();
							if (BackgroundThread.isHTTPAvailable()) {
								new HttpDownloadTask().execute(userString);
							} else {
								Toast.makeText(getApplicationContext(),
										"Connection Unavaliable",
										Toast.LENGTH_LONG).show();
							}
						}
					});
			builder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});

			builder.create().show();
		}
	};

	public void updateListAdapter(Context context) {
		podcastDao.open();
		Cursor podcastCursor = podcastDao.getAllPodcastsAsCursor();
		PodcastAdapter cursorAdapter = new PodcastAdapter(this, podcastCursor,
				0);
		setListAdapter(cursorAdapter);
	}

	private class HttpDownloadTask extends
			AsyncTask<String, Void, ContentValues> {
		long id;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			podcastDao.open();
			id = podcastDao.createAndInsertPodcast("Loading ...").get_id();
			updateListAdapter(getApplicationContext());
		}

		@Override
		protected ContentValues doInBackground(String... urls) {

			BufferedReader reader = null;
			ContentValues podcastData = null;
			try {
				URL url = new URL(urls[0]);
				HttpURLConnection con = (HttpURLConnection) url
						.openConnection();
				InputStream is = con.getInputStream();
				reader = new BufferedReader(new InputStreamReader(is));
				// Pass reader to parser
				// podcastData = RssParser.parsePodcastFromXml(reader);
				podcastData = RssParser.parsePodcastFromXml(is);
				// Add podcast url to content value
				podcastData.put(PodcastSqlHelper.COLUMN_LINK, urls[0]);
			} catch (MalformedURLException e) {
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
		protected void onPostExecute(ContentValues result) {
			podcastDao.open();

			// Delete the placeholder "Loading ..." item
			podcastDao.deletePodcast(id);
			if (result != null) {
				Podcast podcast = podcastDao.insertPodcast(result);
				String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + podcast.getTitle().replaceAll("\\W+", "");
				filePath = filePath + ".png";
				File file = new File(filePath);
				podcast.setImagePath(filePath);
				podcastDao.updatePodcastImagelink(podcast);
				BackgroundThread bt = new BackgroundThread(
						getApplicationContext());
				bt.getEpisodesFromBackgroundThread(podcast.getLink(),
						podcast.get_id());
				bt.getPodcastImageFromBackgroundThread(podcast.getLink(), podcast.getImagePath(),
						podcast.get_id());
				Log.d(TAG, "parsing for episodes");
				// new ParseXmlForEpisodes().execute(new String[]
				// {podcast.getLink(), String.valueOf(podcast.get_id())});
			} else {
				Toast.makeText(getApplicationContext(),
						"Podcast subscription failed: Please check url",
						Toast.LENGTH_LONG).show();
			}
			updateListAdapter(getApplicationContext());

		}

	}

	OnItemClickListener channelListViewOnClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1,
				int itemPosition, long itemId) {
			// Helper.getNewEpisodesFromPodcast(getActivity() , itemId);
			Intent intent = new Intent(PodcastActivity.this,
					ChannelActivity.class);
			intent.putExtra("channel_id", itemId);
			startActivity(intent);
		}
	};

	private long dId;
	private Context mContext = this;
	OnItemLongClickListener channelListViewOnItemLongClickListener = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int itemPosition, long itemId) {
			dId = itemId;
			AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext);
			mBuilder.setTitle("Delete?")
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// NO-OP

								}
							})
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									deletePodcast(dId);
								}
							});
			mBuilder.show();

			return false;
		}
	};

	public void deletePodcast(long itemId) {
		podcastDao.open();
		podcastDao.deletePodcast(itemId);
		updateListAdapter(getApplicationContext());
	}

	private void initialize() {
		ListView listView = getListView();
		listView.setOnItemClickListener(channelListViewOnClickListener);
		listView.setOnItemLongClickListener(channelListViewOnItemLongClickListener);
	}

}
