package fireminder.podcastcatcher.activities;

import java.io.ByteArrayInputStream;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import fireminder.podcastcatcher.BackgroundThread;
import fireminder.podcastcatcher.Playlist;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.db.Episode;
import fireminder.podcastcatcher.db.EpisodeDAO;
import fireminder.podcastcatcher.db.Podcast;
import fireminder.podcastcatcher.db.PodcastDAO;
import fireminder.podcastcatcher.ui.EpisodeAdapter;

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
	Integer podcast_id;
	ImageButton play_btn;

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
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		edao.close();
	}

	/*
	 * Find corresponding view ids for the layout Query podcast db via podcast
	 * data-access object Query episode db set Listeners
	 */
	private void setupViews() {
		Intent intent;
		long id;
		PodcastDAO pdao;
		Podcast podcast;

		intent = getIntent();
		id = intent.getLongExtra("channel_id", 0);

		podcast = getPodcast(id);

		edao = new EpisodeDAO(this);
		edao.open();

		podcast_id = podcast.get_id();

		title_tv.setText(podcast.getTitle());

		try {
			ByteArrayInputStream is = new ByteArrayInputStream(
					podcast.getImagelink());
			Bitmap image = BitmapFactory.decodeStream(is);
			image_iv.setImageBitmap(image);
		} catch (Exception e) {
			image_iv.setImageResource(R.drawable.ic_launcher);
			e.printStackTrace();
		}

		getListView().setOnItemClickListener(new OnItemClickListener() {

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

					//Add episode to playlist

					Playlist.instance.songList.add(_episode);
					for(Episode e : Playlist.instance.songList){
						Log.e(TAG, e.getTitle());
					}
					Toast.makeText(getApplicationContext(), "Playing...",
							Toast.LENGTH_LONG).show();
				}

			}

		});

		registerForContextMenu(getListView());
		updateListAdapter(this, podcast.get_id());

	}

	private Podcast getPodcast(long id) {
		PodcastDAO pdao;
		Podcast podcast;
		pdao = new PodcastDAO(getApplicationContext());
		pdao.open();
		podcast = pdao.getPodcast(id);
		pdao.close();
		return podcast;
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
