package fireminder.podcastcatcher;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class PlayerFragment extends Fragment{

	MediaPlayer player;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		player = new MediaPlayer();
		
		super.onActivityCreated(savedInstanceState);
		
	}
	
	
	public void playSong(String songPath){
		try{
			Uri uri = Uri.parse(songPath);
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.setDataSource(getActivity(), uri);
			player.prepare();
			player.start();
			
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.playerfragment, container, false);
		Button button = (Button) rootView.findViewById(R.id.stop_btn);
		button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent("fireminder.podcastcatcher.PlaybackService");
				intent.setAction(PlaybackService.ACTION_STOP);
				getActivity().startService(intent);
			}
		});
		return rootView;
	}
	
}
