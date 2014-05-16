package fireminder.podcastcatcher.activities;

import java.io.File;
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
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;

import fireminder.podcastcatcher.OnTaskCompleted;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.SettingsManager;
import fireminder.podcastcatcher.db.EpisodeDao;
import fireminder.podcastcatcher.db.PodcastDao;
import fireminder.podcastcatcher.downloads.BackgroundThread;
import fireminder.podcastcatcher.fragments.ChannelFragment;
import fireminder.podcastcatcher.fragments.PlayerFragment;
import fireminder.podcastcatcher.fragments.PlayerLargeFragment;
import fireminder.podcastcatcher.fragments.PlaylistFragment;
import fireminder.podcastcatcher.fragments.PodcastFragment;
import fireminder.podcastcatcher.fragments.RecentFragment;
import fireminder.podcastcatcher.fragments.SettingsFragment;
import fireminder.podcastcatcher.services.ADownloadService;
import fireminder.podcastcatcher.services.BootService;
import fireminder.podcastcatcher.services.PlaybackService;
import fireminder.podcastcatcher.ui.NavAdapter;
import fireminder.podcastcatcher.utils.Helper;
import fireminder.podcastcatcher.utils.Utils;
import fireminder.podcastcatcher.valueobjects.Episode;
import fireminder.podcastcatcher.valueobjects.Podcast;

public class MainActivity extends Activity implements OnTaskCompleted,
        PanelSlideListener {

    private Uri data = null;

    public static final String EPISODE_PLAYING = "episode_playing";

    static PodcastFragment podcastFragment;
    static PlayerFragment playerFragment;
    static PlayerLargeFragment playerLargeFragment;
    static RecentFragment mRecentFragment;
    static PlaylistFragment mPlaylistFragment;
    ChannelFragment mChannelFragment;
    DrawerLayout nav;
    ActionBarDrawerToggle abdt;
    BroadcastReceiver mReceiver;
    private long mEpisodeId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        setContentView(R.layout.activity_main);

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                if (arg1.getAction().matches(PlaybackService.TIME_INTENT)) {
                    playerLargeFragment.updateTime(arg1.getIntExtra(
                            PlaybackService.TIME_EXTRA, 0));
                } else if (arg1.getAction().matches(PlaybackService.MAX_INTENT)) {
                    playerLargeFragment.setMaxTime(arg1.getIntExtra(
                            PlaybackService.MAX_EXTRA, 0));
                    Log.e("HAPT",
                            "sentEpisodeMax ACTIVITY "
                                    + arg1.getIntExtra(
                                            PlaybackService.MAX_EXTRA, 0));
                } else if (arg1.getAction().matches(
                        PlaybackService.EPISODE_CHANGE_INTENT)) {
                    long id = arg1.getLongExtra(
                            PlaybackService.EPISODE_ID_EXTRA, 0);
                    setupPlayerAndDisplayEpisode(id);
                } else if (arg1.getAction().matches(
                        DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                    Utils.log("Download of "
                            + arg1.getLongExtra(
                                    DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                            + "complete");
                    try {
                        mChannelFragment.notifyDataSetChanged();
                    } catch (Exception e) { // may not be active
                    }
                } else if (arg1.getAction().matches(
                        DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
                    DownloadManager dm = (DownloadManager) arg0
                            .getSystemService(DOWNLOAD_SERVICE);
                    for (int i = 0; i < 1500; i++) {
                        try {
                            dm.remove(i);
                        } catch (Exception e) {
                        }
                    }
                }
            }

        };

        // Initialize child fragments
        Uri data = getIntent().getData();

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

        findViewById(R.id.lower_container).setVisibility(View.GONE);
        // UI Listeners
        ((SlidingUpPanelLayout) findViewById(R.id.sliding_layout))
                .setPanelSlideListener(this);
        // DrawerListener

        Intent updateIntent = new Intent(MainActivity.this, BootService.class);
        startService(updateIntent);

        AlarmManager alarmManager = (AlarmManager) this
                .getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, ADownloadService.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i,
                PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pi);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, 10000, pi);

        int hoursBetweenUpdates = Integer.parseInt(PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext())
                .getString(
                        getResources().getString(R.string.prefSyncFrequency),
                        Utils.DEFUALT_UPDATE));

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar
                .getInstance().getTimeInMillis(),
                hoursBetweenUpdates * 60 * 60 * 1000, pi);

        this.startService(new Intent(this, PlaybackService.class));

        final String[] navigationItems = new String[] { "Recent", "Library",
                "Playlist", "Find New", "Settings" };
        ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);
        nav = (DrawerLayout) findViewById(R.id.drawer_layout);

        abdt = new ActionBarDrawerToggle(this, nav, R.drawable.ic_drawer,
                R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerClosed(View arg0) {
                super.onDrawerClosed(arg0);
            }

            @Override
            public void onDrawerOpened(View arg0) {
                super.onDrawerOpened(arg0);

            }

            @Override
            public void onDrawerSlide(View arg0, float slideOffset) {
                int alpha = (int) (slideOffset * 255);
                // getActionBar().setBackgroundDrawable(new
                // ColorDrawable(Color.argb(alpha, 86, 116, 185)));
            }

            @Override
            public void onDrawerStateChanged(int arg0) {
            }

        };

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        nav.setDrawerListener(abdt);
        nav.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT);
        final NavAdapter navAdapter = new NavAdapter(this, R.layout.list_item_nav, navigationItems); 
        mDrawerList.setAdapter(navAdapter);
        //mDrawerList.setAdapter(new ArrayAdapter<String>(this,
        //        android.R.layout.simple_list_item_1, navigationItems));
        mDrawerList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view,
                    int position, long id) {
                navAdapter.setBold(position);
                switch (position) {
                case 0:
                    setRecentFragment();
                    break;
                case 1:
                    setPodcastFragment();
                    break;
                case 2:
                    setPlaylistFragment();
                    break;
                case 3:
                    Intent i = new Intent(getApplicationContext(),
                            SearchActivity.class);
                    startActivityForResult(i, 42);
                    break;
                case 4:
                    startPreferenceFragment();
                    break;
                }
                nav.closeDrawer(Gravity.LEFT);
            }
        });

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        abdt.syncState();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter ifi = new IntentFilter();
        ifi.addAction(PlaybackService.TIME_INTENT);
        ifi.addAction(PlaybackService.MAX_INTENT);
        ifi.addAction(PlaybackService.EPISODE_CHANGE_INTENT);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mReceiver, ifi);
        registerReceiver(mReceiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        String s = preferences.getString("prefSyncFrequency", "");
        Log.e(Utils.TAG, "Prerfences: " + s);
    }

    @Override
    protected void onStop() {
        Intent intent = new Intent(this, PlaybackService.class);
        intent.setAction(PlaybackService.FOREGROUND_OFF_ACTION);
        Log.e(Utils.TAG, "Sending foreground off");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        unregisterReceiver(mReceiver);

        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        mEpisodeId = settings.getLong(EPISODE_PLAYING, -1);
        if (mEpisodeId != -1) {
            setupPlayerAndDisplayEpisode(mEpisodeId);
        }
//        getActionBar().setDisplayUseLogoEnabled(true);
 //       getActionBar().setLogo(R.drawable.ic_white_icon);

        // Stop notification
        Intent intent = new Intent(this, PlaybackService.class);
        intent.setAction(PlaybackService.FOREGROUND_OFF_ACTION);
        startService(intent);

    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, PlaybackService.class);
        intent.setAction(PlaybackService.STOP_ACTION);
        startService(intent);
    }

    @Override
    protected void onPause() {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(EPISODE_PLAYING, playerLargeFragment.getCurrentEpisode());
        editor.commit();

        // Launch notification
        Intent intent = new Intent(this, PlaybackService.class);
        intent.setAction(PlaybackService.FOREGROUND_ON_ACTION);
        startService(intent);

        Intent intent2 = new Intent(this, PlaybackService.class);
        intent2.setAction(PlaybackService.STOP_ACTION);
        startService(intent2);
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

    public void setPodcastFragment() {
        FragmentTransaction trans = getFragmentManager().beginTransaction();
        trans.replace(R.id.fragment_container, podcastFragment);
        trans.addToBackStack(null);
        trans.commit();
    }

    public void setRecentFragment() {
        mRecentFragment = RecentFragment.newInstance();
        FragmentTransaction trans = getFragmentManager().beginTransaction();
        trans.replace(R.id.fragment_container, mRecentFragment);
        trans.addToBackStack(null);
        trans.commit();
    }

    public void setPlaylistFragment() {
        mPlaylistFragment = PlaylistFragment.newInstance();
        FragmentTransaction trans = getFragmentManager().beginTransaction();
        trans.replace(R.id.fragment_container, mPlaylistFragment);
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
        case android.R.id.home:
            if (nav.isDrawerOpen(Gravity.LEFT)) {
                nav.closeDrawer(Gravity.LEFT);
            } else {
                nav.openDrawer(Gravity.LEFT);
            }
            return true;
        case R.id.subscribe_setting:
            podcastFragment.subscribe("http://");
            return true;
        case R.id.download_service:
            Intent intent = new Intent(this, ADownloadService.class);
            this.startService(intent);
            return true;
        case R.id.import_opml:
            importFromOpml();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 42 && resultCode == RESULT_OK) {
            BackgroundThread bt = new BackgroundThread(this);
            String[] podcasts = data.getStringArrayExtra("result");
            for (String p : podcasts) {
                Log.e("MainActivity", "Loading podcast: " + p);
                bt.subscribeToPodcast(p, this);
            }
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

    private void setupPlayerAndDisplayEpisode(long episodeId) {
        try {
            Episode episode = new EpisodeDao(MainActivity.this).get(episodeId);
            Podcast podcast = new PodcastDao(MainActivity.this).get(episode
                    .getPodcast_id());
            playerLargeFragment.setEpisode(episode, podcast);
            findViewById(R.id.lower_container).setVisibility(View.VISIBLE);

            Intent intent = new Intent(this, PlaybackService.class);
            startService(intent);
        } catch (Exception e) {
            mEpisodeId = -1;
        }
    }

    public void startPlayingEpisode(Episode episode, Podcast podcast) {
        playerLargeFragment.setEpisode(episode, podcast);
        findViewById(R.id.lower_container).setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, PlaybackService.class);
        intent.setAction(PlaybackService.START_ACTION);
        intent.putExtra(PlaybackService.EPISODE_EXTRA, episode.get_id());
        startService(intent);
    }

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
        playerLargeFragment.setHeaderVisible(true);

    }

    @Override
    public void onPanelExpanded(View panel) {
        ((SlidingUpPanelLayout) findViewById(R.id.sliding_layout))
                .setDragView(playerLargeFragment.header);
        playerLargeFragment.setHeaderVisible(false);
    }

    @Override
    public void onPanelAnchored(View panel) {
        showToast("Panel Anchored");

    }

    public void onSharedPreferenceChanged(SharedPreferences preference,
            String key) {
        SettingsManager.onSettingsChangeListener(this, preference, key);
    }

    private void importFromOpml() {
        File file = new File(Environment.getExternalStorageDirectory(),
                "podkicker_backup.opml");
        Helper.parseOpmlForPodcasts(file, this);
    }
}
