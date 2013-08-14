package fireminder.podcastcatcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

public class PlayerFragment extends Fragment {

	private static String TAG = PlayerFragment.class.getSimpleName();

	public static Button play_btn;
	public static SeekBar seekBar;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart(){
		super.onStart();
		Context context = getActivity();
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
		play_btn = (Button) rootView.findViewById(R.id.play_btn);
		play_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
		Button pause_btn = (Button) rootView.findViewById(R.id.pause_btn);
		pause_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
		
		seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
		
		return rootView;
	}

	private ServiceConnection serviceConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

	};
}
