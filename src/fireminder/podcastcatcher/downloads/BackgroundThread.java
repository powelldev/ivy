package fireminder.podcastcatcher.downloads;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.http.util.ByteArrayBuffer;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import fireminder.podcastcatcher.OnTaskCompleted;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.db.EpisodeDao2;
import fireminder.podcastcatcher.db.PodcastDao2;
import fireminder.podcastcatcher.utils.Helper;
import fireminder.podcastcatcher.utils.RssParser;
import fireminder.podcastcatcher.valueobjects.Episode;
import fireminder.podcastcatcher.valueobjects.Podcast;

/***
 * Encapsulation of the AsyncTasks this application utilizes.
 */
public class BackgroundThread {

	final static String TAG = BackgroundThread.class.getSimpleName();
	private Context context;
	PodcastDao2 pdao = new PodcastDao2();
	EpisodeDao2 edao = new EpisodeDao2();

	public BackgroundThread(Context context) {
		this.context = context;
	}

	/***
	 * Checks if internet connection is available by querying google.com
	 */
	public static boolean isHTTPAvailable() {
		try {
			URL url = new URL("http://www.google.com");
			HttpURLConnection urlConn = (HttpURLConnection) url
					.openConnection();
			urlConn.setConnectTimeout(1000);
			urlConn.getContent();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/***
	 * launches AsyncTasks for adding episodes to episode database
	 * 
	 * @param url
	 * @param id
	 */
	public void getEpisodesFromBackgroundThread(String url, long id) {
		new ParseXmlForEpisodes().executeOnExecutor(
				AsyncTask.THREAD_POOL_EXECUTOR, new String[] { url, "" + id });
		// new ParseXmlForEpisodes().execute(new String[] { url, "" + id });
	}

	/***
	 * launches AsyncTask for loading a podcast image
	 * 
	 * @param url
	 * @param id
	 */
	public void getPodcastImageFromBackgroundThread(String url,
			long id) {
		//new ParseXmlForImage().executeOnExecutor(
		//		AsyncTask.THREAD_POOL_EXECUTOR, new String[] { url, filePath,
		//				"" + id });
		 new ParseXmlForImage().execute(new String[] { url, "" + id });
	}


	/***
	 * Downloads the given episodes enclosure file to
	 * Environment.DIRECTORY_PODCASTS.
	 */
	public void downloadEpisodeMp3(Episode e) {
		// TODO check if episode .mp3 exists already, if so, no need to download
		boolean exists = false;
		String fileName = e.getUrl();
		fileName = fileName.substring(fileName.lastIndexOf("/"));
		try {
			File testFile = new File(fileName);
			if (testFile.exists())
				exists = true;
		} catch (NullPointerException npe) {
			// Only thrown if file hasn't been created yet
		}
		Log.d(TAG, fileName);

		if (!exists) {
			DownloadManager dm = (DownloadManager) context
					.getSystemService(Context.DOWNLOAD_SERVICE);
			Request request = new Request(Uri.parse(e.getUrl()));
			request.setTitle(e.getTitle())
					.setDescription("Touch to Cancel")
					.setNotificationVisibility(
							Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
					.setDestinationInExternalPublicDir(
							Environment.DIRECTORY_PODCASTS, fileName);
			long enqueue = dm.enqueue(request);
		}
		e.setMp3(Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_PODCASTS + fileName);
		edao.update(e);
	}

	/***
	 * AsyncTask responsible for downlaoding an MP3.
	 */
	private class Mp3DownloadTask extends
			AsyncTask<Episode, Void, ByteArrayBuffer> {

		protected void onPreExecute() {
		}

		protected ByteArrayBuffer doInBackground(Episode... episodes) {

			Episode episode = episodes[0];
			File file = new File(Environment.DIRECTORY_PODCASTS
					+ episode.getTitle() + ".mp3");

			if (!file.exists()) {
				DownloadManager dm = (DownloadManager) context
						.getSystemService(Context.DOWNLOAD_SERVICE);
				Request request = new Request(Uri.parse(episode.getUrl()));
				request.setTitle(episode.getTitle())
						.setDescription("Touch to Cancel")
						.setDestinationInExternalPublicDir(
								Environment.DIRECTORY_PODCASTS,
								episode.getTitle() + ".mp3");
				long enqueue = dm.enqueue(request);
			} else {
				Log.d("Mp3 already exists", Environment.DIRECTORY_PODCASTS
						+ episode.getTitle() + ".mp3");
			}
			return null;
		}

		protected void onPostExecute(ByteArrayBuffer result) {

		}
	}

	/***
	 * AsyncTask responsible for downloading a webpage
	 */
	

	/***
	 * AsyncTask responsible for parsing xml page for image
	 */
	private class ParseXmlForImage extends
			AsyncTask<String, Void, ByteArrayBuffer> {
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
				HttpURLConnection con = (HttpURLConnection) url
						.openConnection();
				InputStream is = con.getInputStream();
				reader = new BufferedReader(new InputStreamReader(is));
				// imagelink = RssParser.parsePodcastImageFromXml(reader);
				imagelink = RssParser.parsePodcastImageFromXml(is);
				// Download image
				URL imageurl = new URL(imagelink);
				HttpURLConnection conn = (HttpURLConnection) imageurl
						.openConnection();
				InputStream istream = conn.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(istream, 128);
				baf = new ByteArrayBuffer(128);
				int current = 0;
				while ((current = bis.read()) != -1) {
					baf.append((byte) current);
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return baf;
		}

		@Override
		protected void onPostExecute(ByteArrayBuffer result) {

			if (result != null) {
				Log.d("Add this to db: ", result + " AT " + idForQuery);
				Podcast podcast = pdao.get(Long.parseLong(idForQuery));
				// podcast.setImagePath(result.toByteArray());
				pdao.update(podcast);
				// BackgroundThread bt = new BackgroundThread(getActivity());
				// bt.getEpisodesFromBackgroundThread(podcast.getLink(),
				// podcast.get_id());
				// new ParseXmlForEpisodes().execute(new String[]
				// {podcast.getLink(), String.valueOf(podcast.get_id())});
			} else {
				Podcast podcast = pdao.get(Long.parseLong(idForQuery));
				Bitmap bmp = BitmapFactory.decodeResource(
						context.getResources(), R.drawable.ic_launcher);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
				byte[] byteArray = stream.toByteArray();
				// podcast.setImagelink(byteArray);
				pdao.update(podcast);
			}

		}

	}

	/*
	 * private class ParseXmlForImageFile extends AsyncTask<String, Void, Void>
	 * {
	 * 
	 * @Override protected void onPostExecute(Void result) {
	 * 
	 * }
	 * 
	 * @Override protected Void doInBackground(String... params) {
	 * ByteArrayOutputStream bytes = null; Bitmap myBitmap; try {
	 * HttpURLConnection input = (HttpURLConnection) new URL(
	 * params[0]).openConnection(); input.setDoInput(true); input.connect();
	 * myBitmap = BitmapFactory.decodeStream(input.getInputStream());
	 * 
	 * bytes = new ByteArrayOutputStream(); } catch (Exception e) { }
	 * 
	 * try { Log.e("***********TAG*********", params[0] + "File name: " +
	 * params[1]); URL url = new URL(params[0]); InputStream is =
	 * url.openStream(); OutputStream os = new FileOutputStream(params[1]);
	 * 
	 * byte[] b = new byte[2048]; int length;
	 * 
	 * while((length = is.read(b)) != -1) { os.write(b, 0, length); }
	 * 
	 * is.close(); os.close();
	 * 
	 * } catch (Exception e){
	 * 
	 * }
	 * 
	 * return null;
	 * 
	 * } }
	 */
	/***
	 * AsyncTask responsible for grabbing individual episodes from a podcast's
	 * RSS.
	 */
	private class ParseXmlForEpisodes extends AsyncTask<String, Void, Void> {
		long idForQuery;

		@Override
		protected Void doInBackground(String... urls) {
			URL url;
			idForQuery = Long.parseLong(urls[1]);
			BufferedReader reader = null;
			List<ContentValues> episodes = null;
			try {
				url = new URL(urls[0]);
				HttpURLConnection con = (HttpURLConnection) url
						.openConnection();
				InputStream is = con.getInputStream();
				reader = new BufferedReader(new InputStreamReader(is));
				Log.d("EpisodeParsing", "here: " + urls[0] + " " + urls[1]);
				// episodes = RssParser.parseEpisodesFromXml(reader,
				// idForQuery);
				episodes = RssParser.parseEpisodesFromXml(is, idForQuery);
				for (ContentValues e : episodes) {
					Log.d("Inserting: ",
							e.getAsString(EpisodeDao2.COLUMN_TITLE));
					Episode episode = new Episode();
					episode.setTitle(e.getAsString(EpisodeDao2.COLUMN_TITLE));
					episode.setDescription(e.getAsString(EpisodeDao2.COLUMN_DESCRIP));
					episode.setUrl(e.getAsString(EpisodeDao2.COLUMN_URL));
					episode.setPubDate(e.getAsLong(EpisodeDao2.COLUMN_PUBDATE));
					episode.setMp3(e.getAsString(EpisodeDao2.COLUMN_MP3));
					episode.setPodcast_id(e.getAsLong(EpisodeDao2.COLUMN_PODCAST_ID));
					edao.insert(episode);
				}

				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		}

	}

	/*** Wrapper for getting new episodes */
	public void getNewEpisodesForPodcast(int podcast_id) {
		new CheckXmlForNewEpisodesForPodcast().executeOnExecutor(
				AsyncTask.THREAD_POOL_EXECUTOR, new Integer[] { podcast_id });
		// new CheckXmlForNewEpisodesForPodcast()
		// .execute(new Integer[] { podcast_id });
	}

	/***
	 * AsyncTask that checks for newest episodes by comparing the latest
	 * episode's pubdate to those in the RSS stream. It ceases looking when it
	 * finds a episode with a date older than that one.
	 */
	private class CheckXmlForNewEpisodesForPodcast extends
			AsyncTask<Integer, Void, Episode[]> {

		@Override
		protected Episode[] doInBackground(Integer... params) {
			Integer podcast_id = params[0];

			Podcast podcast = pdao.get(podcast_id);
			Episode latest = edao.getLatestEpisode(podcast_id);

			// For each Podcast, get the latest episode's pubDate

			List<ContentValues> episodes = null;
			Log.d(TAG, "Latest Epi:" + latest.getTitle() + latest.getPubDate());
			try {

				URL url = new URL(podcast.getLink());
				HttpURLConnection urlConn = (HttpURLConnection) url
						.openConnection();
				InputStream is = urlConn.getInputStream();

				episodes = RssParser.parseNewEpisodesFromXml(is,
						podcast.getId(), latest.getPubDate());
				// TODO WORKING HERE
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (episodes != null) {
				for (ContentValues cv : episodes) {
					Episode episode = new Episode();
					episode.setTitle(cv.getAsString(EpisodeDao2.COLUMN_TITLE));
					episode.setDescription(cv.getAsString(EpisodeDao2.COLUMN_DESCRIP));
					episode.setUrl(cv.getAsString(EpisodeDao2.COLUMN_URL));
					episode.setPubDate(cv.getAsLong(EpisodeDao2.COLUMN_PUBDATE));
					episode.setMp3(cv.getAsString(EpisodeDao2.COLUMN_MP3));
					episode.setPodcast_id(cv.getAsLong(EpisodeDao2.COLUMN_PODCAST_ID));
					Episode le = edao.get(edao.insert(episode));
					Log.d(TAG, le.getTitle() + " " + le.getPubDate());
				}
			}
			// Open Podcast's URL
			// Parse Podcast's url for episodes until an episodes pubDate is <=
			// current latest pubDate
			return (Episode[]) episodes.toArray();
		}

	}

	public void getNewEpisodes() {
		new CheckXmlForNewEpisodes().execute();
	}

	private class CheckXmlForNewEpisodes extends
			AsyncTask<Void, Void, Episode[]> {

		@Override
		protected Episode[] doInBackground(Void... params) {

			Cursor cursor = null;
			// Get a List of Podcasts
			cursor = pdao.getAllPodcastsAsCursor();

			cursor.moveToFirst();

			List<ContentValues> episodes = null;
			do {
				Episode e = null;
				e = edao.getLatestEpisode(cursor.getLong(cursor
						.getColumnIndex(PodcastDao2.COLUMN_ID)));
				Log.e(TAG,
						""
								+ cursor.getString(cursor
										.getColumnIndex(PodcastDao2.COLUMN_LINK)));

				try {
					URL url = new URL(cursor.getString(cursor
							.getColumnIndex(PodcastDao2.COLUMN_LINK)));
					HttpURLConnection urlConn = (HttpURLConnection) url
							.openConnection();
					InputStream is = urlConn.getInputStream();

					episodes = RssParser
							.parseNewEpisodesFromXml(
									is,
									cursor.getInt(cursor
											.getColumnIndex(PodcastDao2.COLUMN_ID)),
									e.getPubDate());
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				if (episodes != null) {
					for (ContentValues cv : episodes) {
					Episode episode = new Episode();
					episode.setTitle(cv.getAsString(EpisodeDao2.COLUMN_TITLE));
					episode.setDescription(cv.getAsString(EpisodeDao2.COLUMN_DESCRIP));
					episode.setUrl(cv.getAsString(EpisodeDao2.COLUMN_URL));
					episode.setPubDate(cv.getAsLong(EpisodeDao2.COLUMN_PUBDATE));
					episode.setMp3(cv.getAsString(EpisodeDao2.COLUMN_MP3));
					episode.setPodcast_id(cv.getAsLong(EpisodeDao2.COLUMN_PODCAST_ID));
					Episode le = edao.get(edao.insert(episode));
						new BackgroundThread(context).downloadEpisodeMp3(le);
						Log.d(TAG, le.getTitle() + " " + le.getPubDate());
					}
				}

			} while (cursor.moveToNext());

			// Open Podcast's URL
			// Parse Podcast's url for episodes until an episodes pubDate is <=
			// current latest pubDate
			Episode[] array = episodes.toArray(new Episode[episodes.size()]);
			return array;
		}

		@Override
		protected void onPostExecute(Episode[] result) {
			super.onPostExecute(result);
			for (Episode e : result) {
				downloadEpisodeMp3(e);
			}
		}
	}

	/***
	 * AsyncTask responsible for searching iTunes podcast directory for a
	 * podcast
	 */
	public class Search extends AsyncTask<String, Void, List<String>> {
		OnTaskCompleted listener;

		public Search(OnTaskCompleted listener) {
			this.listener = listener;
		}

		@Override
		protected List<String> doInBackground(String... params) {
			List<String> results = null;

			InputStream is = null;
			try {
				URL url = new URL(params[0]);
				Log.e(TAG, params[0]);
				HttpURLConnection urlConn = (HttpURLConnection) url
						.openConnection();
				urlConn.setConnectTimeout(10000);
				is = urlConn.getInputStream();
			} catch (java.net.SocketTimeoutException se) {
				return null;
			} catch (Exception e) {
				e.printStackTrace();
			}
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(is));
			} catch (Exception e) {
				return null;
			}
			results = Helper.parseJSONforPodcasts(reader);
			return results;
		}

		@Override
		public void onPostExecute(List<String> result) {
			if (result == null) {
				Toast.makeText(context, "Not found", Toast.LENGTH_LONG).show();
			}
			listener.onTaskCompleted(result);

		}

	}

	/*** Wrapper for iTunes Search AsyncTask */
	public void searchItunesForPodcasts(String searchURL) {
		new Search((OnTaskCompleted) context)
				.execute(new String[] { searchURL });

	}

	public void downloadAll(long podcast_id) {
		List<Episode> episodes = edao.getAllEpisodes(podcast_id);
		for (Episode episode : episodes) {
			downloadEpisodeMp3(episode);
		}
	}

}
