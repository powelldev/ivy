package fireminder.podcastcatcher.activities;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.Context;
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
import android.widget.Toast;
import fireminder.podcastcatcher.OnTaskCompleted;
import fireminder.podcastcatcher.PodcastCatcher;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.boot.BootService;
import fireminder.podcastcatcher.db.EpisodeDao2;
import fireminder.podcastcatcher.downloads.ADownloadService;
import fireminder.podcastcatcher.downloads.BackgroundThread;
import fireminder.podcastcatcher.fragments.PodcastFragment;

public class MainActivity extends FragmentActivity implements OnTaskCompleted {
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		PodcastCatcher.getInstance().setContext(this);
		PodcastCatcher.getInstance().setActivity(this);

		// updateSongList();
		
		Intent updateIntent = new Intent(MainActivity.this, BootService.class);
		startService(updateIntent);

		Intent intent = getIntent();
		data = intent.getData();

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(this, ADownloadService.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
		alarmManager.cancel(pi);
		alarmManager.set(AlarmManager.ELAPSED_REALTIME, 10000, pi);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), AlarmManager.INTERVAL_HOUR, pi);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
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
			case 2:
				return new DummySectionFragment();
			}
			return null;
		}

		@Override
		public int getCount() {
			// Show 0 total pages.
			return 1;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_podcast).toUpperCase(l);
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
			/*
			 * dummyTextView.setText(Integer.toString(getArguments().getInt(
			 * ARG_SECTION_NUMBER)));
			 */
			return rootView;
		}
	}

	@Override
	public void onTaskCompleted(List<String> result) {
		podcastFragment.updateListAdapter();
	}
}
