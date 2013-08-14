package fireminder.podcastcatcher.activities;

import java.util.Locale;

import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import fireminder.podcastcatcher.BackgroundThread;
import fireminder.podcastcatcher.PlaySongCallback;
import fireminder.podcastcatcher.PlaybackService;
import fireminder.podcastcatcher.PlayerFragment;
import fireminder.podcastcatcher.PodcastFragment;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.db.Episode;
import fireminder.podcastcatcher.db.Playlist;
import fireminder.podcastcatcher.db.PlaylistDao;

public class MainActivity extends FragmentActivity implements PlaySongCallback {
	Uri data = null;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	static PodcastFragment podcastFragment;
	static PlayerFragment playerFragment;

	Playlist playlist;
	PlaylistDao dao;

	Intent playerService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// updateSongList();

		Intent intent = getIntent();
		data = intent.getData();

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		ActionBar actionBar = getActionBar();

		playerService = new Intent(this, PlaybackService.class);
	}

	@Override
	protected void onResume() {
		super.onResume();
		playlist = Playlist.instance;
		dao = new PlaylistDao(this);
		dao.open();
	}

	@Override
	protected void onPause() {
		dao.insertPlaylist(playlist);
		dao.close();
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.subscribe_setting:
			podcastFragment.subscribe("http://");
			return true;
		case R.id.test:
			// Intent i = new Intent(this, SearchActivity.class);
			// startActivityForResult(i, 42);
			// Intent playIntent = new
			// Intent("fireminder.podcastcatcher.PlaybackService");
			// playIntent.setAction(PlaybackService.ACTION_PLAY);
			// playIntent.putExtra("songPath",
			// "/mnt/sdcard/Podcasts/freakonomics_podcast052313.mp3");
			// startService(playIntent);
			// Log.d("intent" , playIntent.toString());
			return true;
		case R.id.get_new:
			BackgroundThread bt = new BackgroundThread(this);
			bt.getNewEpisodes();
			return true;
		case R.id.search:
			Intent i = new Intent(this, SearchActivity.class);
			startActivityForResult(i, 42);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 42 && resultCode == RESULT_OK) {
			// podcastFragment.subscribe(data.getStringExtra("result"));
			Toast.makeText(this, data.getStringExtra("result"),
					Toast.LENGTH_LONG).show();
			podcastFragment.subscribe(data.getStringExtra("result"));
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			switch (position) {
			case 0:
				if (data != null) {
					Bundle bundle = new Bundle();
					bundle.putString("uri", data.toString());
					podcastFragment = new PodcastFragment();
					podcastFragment.setArguments(bundle);
					return podcastFragment;
				} else {
					return podcastFragment = new PodcastFragment();
				}
			case 1:
				startService(playerService);
				return playerFragment = new PlayerFragment();
			case 2:
				return new DummySectionFragment();
			}
			return null;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_podcast).toUpperCase(l);
			case 1:
				return "PLAYER";
			case 2:
				return "PLAYLIST";
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_dummy,
					container, false);
			TextView dummyTextView = (TextView) rootView
					.findViewById(R.id.section_label);
			/*
			 * dummyTextView.setText(Integer.toString(getArguments().getInt(
			 * ARG_SECTION_NUMBER)));
			 */
			return rootView;
		}
	}

	@Override
	public void playSongString(String songPath) {
		// TODO Auto-generated method stub

	}

	public void changeSong(Episode current) {
		// TODO Auto-generated method stub
		// Update Now playing: current

	}

}