package fireminder.podcastcatcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import fireminder.podcastcatcher.PlaybackService.MyBinder;

public class PlayerFragment extends Fragment {
	private static String TAG = PlayerFragment.class.getSimpleName();
	boolean bound;
	MediaPlayer player;
	PlaybackService pService;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		bound = false;
		getActivity().bindService(
				new Intent("fireminder.podcastcatcher.PlaybackService"),
				serviceConn, Context.BIND_AUTO_CREATE);
		super.onActivityCreated(savedInstanceState);

	}

	public void playSong(String songPath) {
		try {
			Uri uri = Uri.parse(songPath);
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.setDataSource(getActivity(), uri);
			player.prepare();
			player.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.playerfragment, container,
				false);
		Button stop_btn = (Button) rootView.findViewById(R.id.stop_btn);
		stop_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				/*
				 * Intent intent = new Intent(
				 * "fireminder.podcastcatcher.PlaybackService");
				 * intent.setAction(PlaybackService.ACTION_STOP);
				 * getActivity().startService(intent);
				 */
				 Intent intent = new Intent(
				 "fireminder.podcastcatcher.PlaybackService");
				 intent.setAction(PlaybackService.ACTION_STOP);
				 getActivity().startService(intent);
				if (bound == true)
					pService.stop();
			}
		});
		Button play_btn = (Button) rootView.findViewById(R.id.play_btn);
		play_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				 Intent intent = new Intent(
				 "fireminder.podcastcatcher.PlaybackService");
				 intent.setAction(PlaybackService.ACTION_PLAY);
				 getActivity().startService(intent);
				if (bound == true)
					pService.resume();
			}
		});
		Button pause_btn = (Button) rootView.findViewById(R.id.pause_btn);
		pause_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				 Intent intent = new Intent(
				 "fireminder.podcastcatcher.PlaybackService");
				 intent.setAction(PlaybackService.ACTION_PAUSE);
				 getActivity().startService(intent);
				if (bound == true)
					pService.pause();
			}
		});
		return rootView;
	}

	private ServiceConnection serviceConn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.e(TAG, "Service Connected");
			MyBinder binder = (MyBinder) service;
			pService = binder.getService();
			bound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			bound = false;

		}

	};
}
