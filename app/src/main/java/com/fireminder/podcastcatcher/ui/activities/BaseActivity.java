package com.fireminder.podcastcatcher.ui.activities;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.mediaplayer.MediaPlayerFragment;
import com.fireminder.podcastcatcher.services.RetrieveRecentEpisodesService;
import com.fireminder.podcastcatcher.sync.StubAccount;
import com.fireminder.podcastcatcher.ui.NavigationDrawerAdapter;
import com.fireminder.podcastcatcher.ui.fragments.PodcastPlaybackFragment;
import com.fireminder.podcastcatcher.ui.fragments.SubscribeDialogFragment;
import com.fireminder.podcastcatcher.utils.Logger;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;


public abstract class BaseActivity extends ActionBarActivity {

  private static final String LOG_TAG = BaseActivity.class.getSimpleName();

  //Navigation navdrawer_item
  private DrawerLayout mDrawerLayout;
  private DrawerLayout.DrawerListener mDrawerListener;

  private SlidingUpPanelLayout mSlidingUpLayout;

  protected static final String[] NAVDRAWER_ITEMS = new String[]{
      "Recently Added",
      "Podcast Channels",
      "Playlist"
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);


    Uri uri = getIntent().getData();
    if (uri != null) {
      presentSubscriptionDialog(uri.toString());
    }

    Intent i = new Intent(this, RetrieveRecentEpisodesService.class);
    startService(i);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    getSupportActionBar().setHomeButtonEnabled(true);
    getSupportActionBar().setDisplayHomeAsUpEnabled(false);

    setupNavDrawer();
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
    mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
          case 0:
            startActivity(new Intent(getApplicationContext(), RecentsActivity.class));
            finish();
            break;
          case 1:
            startActivity(new Intent(getApplicationContext(), PodcastsActivity.class));
            finish();
            break;
          case 2:
            startActivity(new Intent(getApplicationContext(), PlaylistActivity.class));
            finish();
            break;
        }
      }
    });

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
          getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_lower, new PodcastPlaybackFragment(), "player").commit();
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

  private void setupNavDrawer() {

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
