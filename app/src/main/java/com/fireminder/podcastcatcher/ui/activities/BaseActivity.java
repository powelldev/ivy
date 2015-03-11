package com.fireminder.podcastcatcher.ui.activities;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.mediaplayer.MediaPlayerService;
import com.fireminder.podcastcatcher.services.RetrieveRecentEpisodesService;
import com.fireminder.podcastcatcher.sync.StubAccount;
import com.fireminder.podcastcatcher.ui.NavigationDrawerAdapter;
import com.fireminder.podcastcatcher.ui.fragments.PodcastPlaybackFragment;
import com.fireminder.podcastcatcher.ui.fragments.SubscribeDialogFragment;
import com.fireminder.podcastcatcher.utils.Logger;
import com.fireminder.podcastcatcher.utils.Utils;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;


public abstract class BaseActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

  private static final String LOG_TAG = BaseActivity.class.getSimpleName();
  private static final String PLAYER_FRAGMENT_TAG = "player";

  private Toolbar mToolbar;

  //Navigation navdrawer_item
  private DrawerLayout mDrawerLayout;
  private DrawerLayout.DrawerListener mDrawerListener;

  private SlidingUpPanelLayout mSlidingUpLayout;

  protected static final String[] NAVDRAWER_ITEMS = new String[]{
      "Recently Added",
      "Podcast Channels",
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);


    Uri uri = getIntent().getData();
    if (uri != null) {
      presentSubscriptionDialog(uri.toString());
    }

  }

  @Override
  protected void onResume() {
    super.onResume();
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
    getSupportFragmentManager().
        beginTransaction().
        replace(R.id.fragment_container_lower, new PodcastPlaybackFragment(), PLAYER_FRAGMENT_TAG)
        .commit();
  }

  @Override
  protected void onPause() {
    super.onPause();
    Intent intent = new Intent(this, MediaPlayerService.class);
    intent.setAction(MediaPlayerService.ACTION_LEAVING);
    startService(intent);
  }


  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    setupSlidingPanel();
    if (mDrawerLayout == null) {
      return;
    }
    mDrawerListener = new DrawerLayout.DrawerListener() {
      @Override
      public void onDrawerSlide(View drawerView, float slideOffset) {

      }

      @Override
      public void onDrawerOpened(View drawerView) {
      }

      @Override
      public void onDrawerClosed(View drawerView) {

      }

      @Override
      public void onDrawerStateChanged(int newState) {

      }
    };

    mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.BOTTOM);
    mDrawerLayout.setDrawerListener(mDrawerListener);

    final ListView mDrawerList = (ListView) findViewById(R.id.drawer_listview);
    final NavigationDrawerAdapter adapter = new NavigationDrawerAdapter(getApplicationContext(), R.layout.drawer_item, NAVDRAWER_ITEMS);
    mDrawerList.setAdapter(adapter);
    mDrawerList.setOnItemClickListener(this);
    if (mToolbar != null) {
      mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          mDrawerLayout.openDrawer(Gravity.START);
        }
      });
    }

  }

  @Override
  public void setContentView(int layoutResID) {
    super.setContentView(layoutResID);
    getToolbar();
  }

  protected Toolbar getToolbar() {
    if (mToolbar == null) {
      mToolbar = (Toolbar) findViewById(R.id.toolbar);
      if (mToolbar != null) {
        setSupportActionBar(mToolbar);
      }
    }
    return mToolbar;
  }


  public void setNavDrawerItems(String[] episodeList) {
    final ListView mDrawerList = (ListView) findViewById(R.id.drawer_listview);
    final NavigationDrawerAdapter adapter = new NavigationDrawerAdapter(getApplicationContext(), R.layout.drawer_item, episodeList);
    mDrawerList.setAdapter(adapter);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  private void setupSlidingPanel() {
    mSlidingUpLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
    if (mSlidingUpLayout != null) {
      mSlidingUpLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
        @Override
        public void onPanelSlide(View panel, float slideOffset) {
          Logger.d(LOG_TAG, "onPanelSlide() + offset: " + slideOffset);
        }

        @Override
        public void onPanelCollapsed(View panel) {
          Logger.d(LOG_TAG, "onPanelCollpased()");

        }

        @Override
        public void onPanelExpanded(View panel) {
          Logger.d(LOG_TAG, "onPanelExpanded()");
          /*
          getSupportFragmentManager().
              beginTransaction().
              replace(R.id.fragment_container_lower, new PodcastPlaybackFragment(), PLAYER_FRAGMENT_TAG)
              .commit();
             */
        }

        @Override
        public void onPanelAnchored(View panel) {
          Logger.d(LOG_TAG, "onPanelAnchored()");
        }

        @Override
        public void onPanelHidden(View panel) {
          Logger.d(LOG_TAG, "onPanelHidden()");
        }
      });
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    getSupportFragmentManager().
        beginTransaction().
        replace(R.id.fragment_container_lower, new PodcastPlaybackFragment(), PLAYER_FRAGMENT_TAG)
        .commit();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        break;
      case R.id.subscribe_setting:
        presentSubscriptionDialog("");
        break;
      case R.id.menu_search:
        startActivity(new Intent(this, SearchActivity.class));
        break;
      case R.id.menu_refresh:
        requestImmediateSync();
        break;
      case R.id.menu_cleanup:
        Utils.cleanUpStorage(getApplicationContext());
      default:
        return super.onOptionsItemSelected(item);
    }
    return true;
  }

  private void requestImmediateSync() {
    Bundle settingsBundle = new Bundle();
    settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
    settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
    Account account = StubAccount.getAccount(this);
    ContentResolver.setIsSyncable(account, StubAccount.AUTHORITY, 1);
    ContentResolver.requestSync(account, StubAccount.AUTHORITY, settingsBundle);
  }

  private void presentSubscriptionDialog(String emptyOrUri) {
    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
    if (prev != null) {
      ft.remove(prev);
    }
    ft.addToBackStack(null);
    SubscribeDialogFragment subscribeDialogFragment = SubscribeDialogFragment.newInstance(emptyOrUri);
    subscribeDialogFragment.show(ft, "dialog");
  }


}
