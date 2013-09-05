package fireminder.podcastcatcher;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import fireminder.podcastcatcher.db.Episode;
import fireminder.podcastcatcher.db.EpisodeDAO;
import fireminder.podcastcatcher.db.PodcastDAO;
import fireminder.podcastcatcher.db.PodcastSqlHelper;

public class DownloadService extends IntentService {

	public DownloadService() {
		super("DownloadService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
		wl.acquire();
		Episode[] result = doInBackground();
		onPostExecute(result);
		wl.release();

	}

	PodcastDAO pdao;
	EpisodeDAO edao;

	protected Episode[] doInBackground() {

		Cursor cursor = null;
		edao = new EpisodeDAO(this);
		pdao = new PodcastDAO(this);
		edao.open();
		// Get a List of Podcasts
		pdao.open();
		cursor = pdao.getAllPodcastsAsCursor();

		cursor.moveToFirst();

		List<ContentValues> cvEpisodes = null;
		List<Episode> episodes = null;
		while (cursor.moveToNext()) {
			Episode e = null;
			e = edao.getLatestEpisode(cursor.getLong(cursor
					.getColumnIndex(PodcastSqlHelper.COLUMN_ID)));
			Log.e("IntentService",
					""
							+ cursor.getString(cursor
									.getColumnIndex(PodcastSqlHelper.COLUMN_LINK)));

			try {
				URL url = new URL(cursor.getString(cursor
						.getColumnIndex(PodcastSqlHelper.COLUMN_LINK)));
				HttpURLConnection urlConn = (HttpURLConnection) url
						.openConnection();
				InputStream is = urlConn.getInputStream();

				cvEpisodes = RssParser.parseNewEpisodesFromXml(is, cursor
						.getInt(cursor
								.getColumnIndex(PodcastSqlHelper.COLUMN_ID)), e
						.getPubDate());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			episodes = new ArrayList<Episode>();
			if (cvEpisodes != null) {
				for (ContentValues cv : cvEpisodes) {
					// TODO Download automatically?
					// Add to playlist?
					Episode le = edao.insertEpisode(cv);
					episodes.add(le);
					new BackgroundThread(this).downloadEpisodeMp3(le);
					Log.d("IntentService", le.getTitle() + " " + le.getPubDate());
				}
			}

		}

		Episode[] array = episodes.toArray(new Episode[episodes.size()]);
		return array;
	}

	protected void onPostExecute(Episode[] result) {
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
		DownloadManager dm = (DownloadManager) this
				.getSystemService(Context.DOWNLOAD_SERVICE);
		Request request = new Request(Uri.parse(e.getUrl()));
		request.setTitle(e.getTitle())
				.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
				.setDescription(e.getDescription())
				.setDestinationInExternalPublicDir(
						Environment.DIRECTORY_PODCASTS, fileName);
		long enqueue = dm.enqueue(request);
		EpisodeDAO edao = new EpisodeDAO(this);
		edao.open();
		edao.updateEpisodeMp3(e.get_id(), Environment
				.getExternalStorageDirectory().getPath()
				+ "/"
				+ Environment.DIRECTORY_PODCASTS + fileName);
		edao.close();
	}

}
