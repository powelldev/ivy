package fireminder.podcastcatcher;

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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import fireminder.podcastcatcher.db.EpisodeDAO;
import fireminder.podcastcatcher.db.Podcast;
import fireminder.podcastcatcher.db.PodcastDAO;
import fireminder.podcastcatcher.ui.LazyAdapter;

/*
 * ChannelActivity displays a podcast's episodes. It gets the
 * episodes by querying the podcastdb by using the id passed 
 * via an intent extra.
 */
public class ChannelActivity extends ListActivity{

	TextView title_tv;
	TextView descrip_tv;
	ImageView image_iv;
	EpisodeDAO edao;
	Integer podcast_id;
	
	@Override
	protected void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.channel);
		findViewsById();
		setupViews();
		
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
		super.onCreateContextMenu(menu, v, menuInfo);
		android.view.MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.episode_menu, (android.view.Menu) menu);
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item){
		Log.d("item", ""+item.getItemId());
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Log.d("item", ""+info.id);
		switch (item.getItemId()){
		case R.id.delete:
			edao.deleteEpisode(info.id);
			updateListAdapter(getApplicationContext(), podcast_id);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	@Override
	protected void onPause(){
		super.onPause();
		edao.close();
	}

	/*
	 * Find corresponding view ids for the layout
	 * Query podcast db via podcast data-access object
	 * Query episode db
	 * set Listeners
	 */
	private void setupViews() {
		Intent intent;
		long id;
		PodcastDAO pdao;
		Podcast podcast;
		
		intent = getIntent();
		id = intent.getLongExtra("channel_id", 0);
		pdao = new PodcastDAO(getApplicationContext());
		pdao.open();
		podcast = pdao.getPodcast(id);
		pdao.close();
		edao = new EpisodeDAO(this);
		edao.open();

		podcast_id = podcast.get_id();
		
		title_tv.setText(podcast.getTitle());
		descrip_tv.setText(podcast.getDescription());
		try {
			ByteArrayInputStream is = new ByteArrayInputStream(podcast.getImagelink());
			Bitmap image = BitmapFactory.decodeStream(is);
			image_iv.setImageBitmap(image);
		} catch (Exception e){
			image_iv.setImageResource(R.drawable.ic_launcher);
			e.printStackTrace();
		}

		/*getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long itemId) {
				
			//	edao.deleteEpisode(itemId);
			//	updateListAdapter(getApplicationContext(), podcast_id);
				return false;
			}
		});*/
		registerForContextMenu(getListView());
		updateListAdapter(this, podcast.get_id());
		
		
	}
	/*
	 * Query episode db with podcast id
	 * Update listview with result
	 */
	private void updateListAdapter(Context context, long id) {
		Cursor episodeCursor = edao.getAllEpisodesAsCursor(id);
		LazyAdapter cursorAdapter = new LazyAdapter(context, episodeCursor);
		//SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(context, android.R.layout.simple_list_item_1,
		//		episodeCursor, new String[] { EpisodeSqlHelper.COLUMN_TITLE }, new int[] { android.R.id.text1 }, 2);
		setListAdapter(cursorAdapter);
		}

	private void findViewsById() {
		title_tv = (TextView) findViewById(R.id.textview);
		descrip_tv = (TextView) findViewById(R.id.podcast_descrip);
		image_iv = (ImageView) findViewById(R.id.podcast_image);
	}
}
