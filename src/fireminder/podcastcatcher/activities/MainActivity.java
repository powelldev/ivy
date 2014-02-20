package fireminder.podcastcatcher.activities;

import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;

import fireminder.podcastcatcher.OnTaskCompleted;
import fireminder.podcastcatcher.PodcastCatcher;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.db.EpisodeDao;
import fireminder.podcastcatcher.db.PodcastDao;
import fireminder.podcastcatcher.downloads.BackgroundThread;
import fireminder.podcastcatcher.fragments.ChannelFragment;
import fireminder.podcastcatcher.fragments.PlayerFragment;
import fireminder.podcastcatcher.fragments.PlayerLargeFragment;
import fireminder.podcastcatcher.fragments.PodcastFragment;
import fireminder.podcastcatcher.fragments.SettingsFragment;
import fireminder.podcastcatcher.services.ADownloadService;
import fireminder.podcastcatcher.services.BootService;
import fireminder.podcastcatcher.services.PlaybackService;
import fireminder.podcastcatcher.utils.Utils;
import fireminder.podcastcatcher.valueobjects.Episode;
import fireminder.podcastcatcher.valueobjects.Podcast;

public class MainActivity extends Activity implements OnTaskCompleted {

    private Uri data = null;

    private static final String ACTION_BAR_STATE_HIDDEN = "saved_state_action_bar";
    private static final String EPISODE_PLAYING = "episode_playing";

    static PodcastFragment podcastFragment;
    static PlayerFragment playerFragment;
    static PlayerLargeFragment playerLargeFragment;
    ChannelFragment mChannelFragment;

    BroadcastReceiver mReceiver;
    private long mEpisodeId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        setContentView(R.layout.activity_main);
        
        Intent intent = getIntent();
        data = intent.getData();
        
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                if (arg1.getAction().matches(PlaybackService.TIME_INTENT)){
                    playerLargeFragment.updateTime(arg1.getIntExtra(PlaybackService.TIME, 0));
                } else if (arg1.getAction().matches(PlaybackService.MAX_INTENT)) {
                    playerLargeFragment.setMaxTime(arg1.getIntExtra(PlaybackService.MAX, 0));
                    Log.e("HAPT", "sentEpisodeMax ACTIVITY " + arg1.getIntExtra(PlaybackService.MAX, 0));
                }
            }
            
        };
        if (data != null) {
            Bundle bundle = new Bundle();
            bundle.putString("uri", data.toString());
            podcastFragment = new PodcastFragment();
            podcastFragment.setArguments(bundle);
        } else {
            podcastFragment = new PodcastFragment();
        }

        playerFragment = new PlayerFragment();
        playerLargeFragment = new PlayerLargeFragment();

        FragmentTransaction trans = getFragmentManager().beginTransaction();
        trans.add(R.id.fragment_container, podcastFragment);
        trans.add(R.id.lower_container, playerLargeFragment);
        trans.commit();

        PodcastCatcher.getInstance().setContext(this);
        PodcastCatcher.getInstance().setActivity(this);

        final SlidingUpPanelLayout layout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        layout.setPanelSlideListener(new PanelSlideListener() {

            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset < 0.4) {
                    if (getActionBar().isShowing()) {
                        getActionBar().hide();
                    }
                } else {
                    if (!getActionBar().isShowing()) {
                        getActionBar().show();
                    }
                }

            }

            @Override
            public void onPanelCollapsed(View panel) {

            }

            @Override
            public void onPanelExpanded(View panel) {
                layout.setDragView(playerLargeFragment.header);
            }

            @Override
            public void onPanelAnchored(View panel) {
                showToast("Panel Anchored");

            }

        });

        Intent updateIntent = new Intent(MainActivity.this, BootService.class);
        startService(updateIntent);

        AlarmManager alarmManager = (AlarmManager) this
                .getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, ADownloadService.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
        alarmManager.cancel(pi);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, 10000, pi);
        // TODO
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar
                .getInstance().getTimeInMillis(), Utils.UPDATE_TIMING,
                pi);

        boolean actionBarHidden = savedInstanceState != null ? savedInstanceState
                .getBoolean(ACTION_BAR_STATE_HIDDEN, false) : false;

        if (actionBarHidden) {
            getActionBar().hide();
        }

        this.startService(new Intent(this, PlaybackService.class));
    }

    @Override
    protected void onStart() {
       super.onStart(); 
       IntentFilter ifi = new IntentFilter();
       ifi.addAction(PlaybackService.TIME_INTENT);
       ifi.addAction(PlaybackService.MAX_INTENT);
       LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, ifi);
    }
    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onStop();
    }
    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putBoolean(ACTION_BAR_STATE_HIDDEN, !getActionBar().isShowing());

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        mEpisodeId = settings.getLong(EPISODE_PLAYING, -1);
        if (mEpisodeId != -1) {
            resumeEpisode(mEpisodeId);
        }
    }

    @Override
    protected void onPause() {
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(EPISODE_PLAYING, playerLargeFragment.getCurrentEpisode());
        editor.commit();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void startPreferenceFragment() {
        FragmentTransaction trans = getFragmentManager().beginTransaction();
        trans.replace(R.id.fragment_container, new SettingsFragment());
        trans.addToBackStack(null);
        trans.commit();
    }

    public void setChannelFragment(long channelId) {
        mChannelFragment = ChannelFragment.newInstance(channelId);
        FragmentTransaction trans = getFragmentManager().beginTransaction();
        trans.replace(R.id.fragment_container, mChannelFragment);
        trans.addToBackStack(null);
        trans.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        case R.id.subscribe_setting:
            podcastFragment.subscribe("http://");
            return true;
        case R.id.download_service:
            Intent intent = new Intent(this, ADownloadService.class);
            this.startService(intent);
            return true;
        case R.id.get_new:
            BackgroundThread bt = new BackgroundThread(this);
            bt.getNewEpisodes();
            return true;
        case R.id.search:
            Intent i = new Intent(this, SearchActivity.class);
            startActivityForResult(i, 42);
            return true;
        case R.id.viewDownloads:
            Intent dlIntent = new Intent();
            dlIntent.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
            startActivity(dlIntent);
            return true;
        case R.id.settings:
            this.startPreferenceFragment();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 42 && resultCode == RESULT_OK) {
            // Search finished, update ListView?
            podcastFragment.updateListAdapter();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onTaskCompleted(List<String> result) {
        podcastFragment.updateListAdapter();
    }

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void resumeEpisode(long episodeId) {
        try {
            Episode episode = new EpisodeDao().get(episodeId);
            Podcast podcast = new PodcastDao().get(episode.getPodcast_id());
            startPlayingEpisode(episode, podcast);
        } catch (Exception e) {
            mEpisodeId = -1;
        }
    }

    public void startPlayingEpisode(Episode episode, Podcast podcast) {
        playerLargeFragment.setEpisode(episode, podcast);
        Intent intent = new Intent(this, PlaybackService.class);
        intent.setAction("START");
        intent.putExtra(PlaybackService.EPISODE_EXTRA, episode.get_id());
        startService(intent);
    }
}
