package fireminder.podcastcatcher.downloads;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import fireminder.podcastcatcher.db.EpisodeDao2;
import fireminder.podcastcatcher.db.PodcastDao2;
import fireminder.podcastcatcher.utils.RssParser;
import fireminder.podcastcatcher.valueobjects.Episode;

public class ADownloadService extends IntentService {
	
	PodcastDao2 pdao = new PodcastDao2();
	EpisodeDao2 edao = new EpisodeDao2();

	public ADownloadService() {
		super("DownloadService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Intent i = new Intent();
				i.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
				startActivity(i);
			}
		};
		
		registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
		Log.e("DOWNLOADSERVICE", "From dls");
		PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
		wl.acquire();
		List<Episode> result = doInBackground();
		onPostExecute(result);
		wl.release();

	}



	protected List<Episode> doInBackground() {

		Cursor cursor = null;
		// Get a List of Podcasts
		cursor = pdao.getAllPodcastsAsCursor();

		cursor.moveToFirst();

		List<ContentValues> cvEpisodes = null;
		List<Episode> episodes = null;
		do {
			Episode e = null;
			e = edao.getLatestEpisode(cursor.getLong(cursor
					.getColumnIndex(PodcastDao2.COLUMN_ID)));
			Log.e("IntentService",
					""
							+ cursor.getString(cursor
									.getColumnIndex(PodcastDao2.COLUMN_LINK)));

			try {
				URL url = new URL(cursor.getString(cursor
						.getColumnIndex(PodcastDao2.COLUMN_LINK)));
				HttpURLConnection urlConn = (HttpURLConnection) url
						.openConnection();
				InputStream is = urlConn.getInputStream();

				cvEpisodes = RssParser.parseNewEpisodesFromXml(is, cursor
						.getInt(cursor
								.getColumnIndex(PodcastDao2.COLUMN_ID)), e
						.getPubDate());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			episodes = new ArrayList<Episode>();
			if (cvEpisodes != null) {
				for (ContentValues cv : cvEpisodes) {
					// TODO Download automatically?
					// Add to playlist?

					Episode episode = new Episode();
					episode.setTitle(cv.getAsString(EpisodeDao2.COLUMN_TITLE));
					episode.setDescription(cv.getAsString(EpisodeDao2.COLUMN_DESCRIP));
					episode.setUrl(cv.getAsString(EpisodeDao2.COLUMN_URL));
					episode.setPubDate(cv.getAsLong(EpisodeDao2.COLUMN_PUBDATE));
					episode.setMp3(cv.getAsString(EpisodeDao2.COLUMN_MP3));
					episode.setPodcast_id(cv.getAsLong(EpisodeDao2.COLUMN_PODCAST_ID));
					Episode le = edao.get(edao.insert(episode));
					episodes.add(le);
					Log.d("IntentService", le.getTitle() + " " + le.getPubDate());
				}
			}

		} while (cursor.moveToNext());
		return episodes;
	}

	protected void onPostExecute(List<Episode> result) {
		if (result == null) { 
			Log.e("DLs", "No New episodes");
			return; 
			}
		for (Episode e : result) {
			downloadEpisodeMp3(e);
		}
	}
	
	public void downloadEpisodeMp3(Episode e) {
		String fileName = e.getUrl();
		fileName = fileName.substring(fileName.lastIndexOf("/"));
		Log.d("Downloading...", fileName);
		// TODO Make download notification point back at app - or allow
		// cessation of download
		final DownloadManager dm = (DownloadManager) this
				.getSystemService(Context.DOWNLOAD_SERVICE);
		
		Request request = new Request(Uri.parse(e.getUrl()));
		request.setTitle(e.getTitle())
				.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
				.setDescription("Touch to cancel")
				.setDestinationInExternalPublicDir(
						Environment.DIRECTORY_PODCASTS, fileName);
		long enqueue = dm.enqueue(request);
		e.setMp3(Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_PODCASTS + fileName);
		edao.update(e);
	}

}
