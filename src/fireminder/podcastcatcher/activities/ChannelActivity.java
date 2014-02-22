package fireminder.podcastcatcher.activities;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.File;

import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.db.EpisodeDao;
import fireminder.podcastcatcher.db.PodcastDao;
import fireminder.podcastcatcher.downloads.BackgroundThread;
import fireminder.podcastcatcher.ui.EpisodeAdapter;
import fireminder.podcastcatcher.utils.Helper;
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
    long mPodcastId;
    ImageButton play_btn;
    
    Cursor mCursor;
    
    EpisodeDao edao = new EpisodeDao();
    
    PodcastDao pdao = new PodcastDao();

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
            edao.delete(edao.get(info.id));
            updateListAdapter(getApplicationContext(), mPodcastId);
            return true;
        case R.id.downloadAll:
            new BackgroundThread(this).downloadAll(mPodcastId);
        default:
            return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateListAdapter(this, mPodcastId);
    }

    /**
     * Find corresponding view ids for the layout Query podcast db via podcast
     * data-access object Query episode db set Listeners
     */
    private void setupViews() {
        Intent intent;
        Podcast podcast;

        intent = getIntent();
        mPodcastId = intent.getLongExtra("channel_id", 0);

        podcast = pdao.get(mPodcastId);

        mPodcastId = podcast.getId();

        title_tv.setText(podcast.getTitle());

        try {
            Bitmap image = BitmapFactory.decodeFile(podcast.getImagePath());
            image_iv.setImageBitmap(image);
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

                _episode = edao.get(episode_id);

                if (_episode.getMp3() == null) {
                    Toast.makeText(getApplicationContext(), "Downloading ...",
                            Toast.LENGTH_SHORT).show();
                    Helper.downloadEpisodeMp3(_episode);
                } else {
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    File file = new File(_episode.getMp3());
                    if (!file.exists()) {
                        Toast.makeText(getApplicationContext(),
                                "Downloading ...", Toast.LENGTH_SHORT).show();
                    Helper.downloadEpisodeMp3(_episode);
                    } else {
                        intent.setDataAndType(Uri.fromFile(file), "audio/*");
                        startActivity(intent);
                        Toast.makeText(getApplicationContext(), "Playing...",
                                Toast.LENGTH_LONG).show();
                    }
                }

            }

        });

        registerForContextMenu(getListView());
        updateListAdapter(this, podcast.getId());

    }

    private void updateListAdapter(Context context, long id) {
        mCursor = edao.getAllEpisodesAsCursorByDate(id);
        EpisodeAdapter cursorAdapter = new EpisodeAdapter(context,
                mCursor, 0);
        setListAdapter(cursorAdapter);
    }

    private void findViewsById() {
        title_tv = (TextView) findViewById(R.id.title_tv);
        image_iv = (ImageView) findViewById(R.id.podcast_image);
        play_btn = (ImageButton) findViewById(R.id.play_icon_iv);
    }
}
