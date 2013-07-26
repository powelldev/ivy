package fireminder.podcastcatcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import fireminder.podcastcatcher.PlaybackService.LocalBinder;

public class PlayerFragment extends Fragment {

	private static String TAG = PlayerFragment.class.getSimpleName();

	PlaybackService pService = null;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart(){
		super.onStart();
		Context context = getActivity();
		Intent intent = new Intent(context, PlaybackService.class);
		context.bindService(intent, serviceConn, Context.BIND_AUTO_CREATE);
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
			}
		});
		Button play_btn = (Button) rootView.findViewById(R.id.play_btn);
		play_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(pService != null){
					pService.playCurrentSong();
				}
			}
		});
		Button pause_btn = (Button) rootView.findViewById(R.id.pause_btn);
		pause_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(pService != null){
					pService.playPauseSong();
				}
			}
		});
		return rootView;
	}

	private ServiceConnection serviceConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			pService = binder.getService();
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			pService = null;
		}

	};
}
