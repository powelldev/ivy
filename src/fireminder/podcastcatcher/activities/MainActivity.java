package fireminder.podcastcatcher.activities;

import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import fireminder.podcastcatcher.OnTaskCompleted;
import fireminder.podcastcatcher.PodcastCatcher;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.boot.BootService;
import fireminder.podcastcatcher.downloads.ADownloadService;
import fireminder.podcastcatcher.downloads.BackgroundThread;
import fireminder.podcastcatcher.fragments.PodcastFragment;
import fireminder.podcastcatcher.fragments.SettingsFragment;

public class MainActivity extends Activity implements OnTaskCompleted {

	private Uri data = null;

	static PodcastFragment podcastFragment;
	ChannelFragment mChannelFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		Intent intent = getIntent();
		data = intent.getData();

		if (data != null) {
			Bundle bundle = new Bundle();
			bundle.putString("uri", data.toString());
			podcastFragment = new PodcastFragment();
			podcastFragment.setArguments(bundle);
		} else {
			podcastFragment = new PodcastFragment();
		}

		FragmentTransaction trans = getFragmentManager().beginTransaction();
		trans.add(R.id.fragment_container, podcastFragment);
		trans.commit();

		PodcastCatcher.getInstance().setContext(this);
		PodcastCatcher.getInstance().setActivity(this);

		Intent updateIntent = new Intent(MainActivity.this, BootService.class);
		startService(updateIntent);

		AlarmManager alarmManager = (AlarmManager) this
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(this, ADownloadService.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
		alarmManager.cancel(pi);
		alarmManager.set(AlarmManager.ELAPSED_REALTIME, 10000, pi);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar
				.getInstance().getTimeInMillis(), AlarmManager.INTERVAL_HOUR,
				pi);
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
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void startPreferenceFragment() {
		FragmentTransaction trans = getFragmentManager()
				.beginTransaction();
		trans.replace(R.id.fragment_container, new SettingsFragment());
		trans.addToBackStack(null);
		trans.commit();
	}
	public void setChannelFragment(long channelId) {
		mChannelFragment = ChannelFragment.newInstance(channelId);
		FragmentTransaction trans = getFragmentManager()
				.beginTransaction();
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
}
