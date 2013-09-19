package fireminder.podcastcatcher.activities;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.db.EpisodeDAO;
import fireminder.podcastcatcher.db.PodcastDao2;
import fireminder.podcastcatcher.downloads.BackgroundThread;
import fireminder.podcastcatcher.ui.EpisodeAdapter;
import fireminder.podcastcatcher.valueobjects.Episode;
import fireminder.podcastcatcher.valueobjects.Podcast;

/*
 * ChannelActivity displays a podcast's episodes. It gets the
 * episodes by querying the podcastdb by using the id passed
 * via an intent extra.
 */
public class ChannelActivity extends ListActivity {

	final static String TAG = ChannelActivity.class.getSimpleName();

	TextView title_tv;
	TextView descrip_tv;
	ImageView image_iv;
	EpisodeDAO edao;
	long podcast_id;
	ImageButton play_btn;
	
	PodcastDao2 pdao = new PodcastDao2();

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.channel);
		findViewsById();
		setupViews();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		// TODO add a Download all
		android.view.MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.episode_menu, (android.view.Menu) menu);
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.delete:
			Log.d("menu delete", "Delete clicked: " + info.id);
			edao.open();
			edao.deleteEpisode(info.id);
			updateListAdapter(getApplicationContext(), podcast_id);
			return true;
		case R.id.downloadAll:
			new BackgroundThread(this).downloadAll(podcast_id);
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		edao.close();
	}

	/**
	 * Find corresponding view ids for the layout Query podcast db via podcast
	 * data-access object Query episode db set Listeners
	 */
	private void setupViews() {
		Intent intent;
		long id;
		Podcast podcast;

		intent = getIntent();
		id = intent.getLongExtra("channel_id", 0);

		podcast = pdao.get(id);

		edao = new EpisodeDAO(this);
		edao.open();

		podcast_id = podcast.getId();

		title_tv.setText(podcast.getTitle());

		try {
			// ByteArrayInputStream is = new ByteArrayInputStream(
			// podcast.getImagePath());
			// Bitmap image = BitmapFactory.decodeStream(is);
			// image_iv.setImageBitmap(image);
		} catch (Exception e) {
			image_iv.setImageResource(R.drawable.ic_launcher);
			e.printStackTrace();
		}

		getListView().setOnItemClickListener(new OnItemClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long episode_id) {
				Episode _episode;
				BackgroundThread bt;

				edao.open();
				_episode = edao.getEpisode(episode_id);

				if (_episode.getMp3() == null) {
					Toast.makeText(getApplicationContext(), "Downloading ...",
							Toast.LENGTH_SHORT).show();
					bt = new BackgroundThread(getApplicationContext());
					bt.downloadEpisodeMp3(_episode);
				} else {
					Intent intent = new Intent();
					intent.setAction(android.content.Intent.ACTION_VIEW);
					File file = new File(_episode.getMp3());
					if (!file.exists()) {
						Toast.makeText(getApplicationContext(),
								"Downloading ...", Toast.LENGTH_SHORT).show();
						bt = new BackgroundThread(getApplicationContext());
						bt.downloadEpisodeMp3(_episode);
					} else {
						intent.setDataAndType(Uri.fromFile(file), "audio/*");
						startActivity(intent);
						Toast.makeText(getApplicationContext(), "Playing...",
								Toast.LENGTH_LONG).show();

						/*
						 * File mfile = new File(_episode.getMp3()); Uri uri =
						 * Uri.fromFile(mfile); Intent playbackIntent = new
						 * Intent( Intent.CATEGORY_APP_MUSIC);
						 * playbackIntent.setAction(Intent.ACTION_VIEW);
						 * playbackIntent.setDataAndType(uri, "audio/*");
						 * startActivity(playbackIntent);
						 */
					}
				}

			}

		});

		registerForContextMenu(getListView());
		updateListAdapter(this, podcast.getId());

	}

	private void updateListAdapter(Context context, long id) {
		Cursor episodeCursor = edao.getAllEpisodesAsCursorByDate(id);
		EpisodeAdapter cursorAdapter = new EpisodeAdapter(context,
				episodeCursor, 0);
		setListAdapter(cursorAdapter);
	}

	private void findViewsById() {
		title_tv = (TextView) findViewById(R.id.title_tv);
		image_iv = (ImageView) findViewById(R.id.podcast_image);
		play_btn = (ImageButton) findViewById(R.id.play_icon_iv);
	}
}
