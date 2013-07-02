package fireminder.podcastcatcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;
import fireminder.podcastcatcher.db.Playlist;
import fireminder.podcastcatcher.db.PlaylistDAO;
import fireminder.podcastcatcher.ui.PlaylistAdapter;

public class PlaylistFragment extends ListFragment implements Runnable{

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unbindService(serverConn);
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().bindService(new Intent(getActivity(), PlaybackService.class), serverConn, Context.BIND_AUTO_CREATE);
		new Thread(this).start();
		
	}


	PlaybackService playbackService = null;
	SeekBar seekBar = null;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.playlistfragment, container, false);
		updateListActivity(getActivity());
		ListView listview = (ListView) rootView.findViewById(android.R.id.list);
		seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long episode_id) {
				PlaylistDAO pdao = new PlaylistDAO(getActivity());
				pdao.open();
				Playlist playlist = new Playlist(pdao.getPlaylist());
				Toast.makeText(getActivity(), "" + playlist.episode_ids.get(arg2), Toast.LENGTH_LONG).show();
				pdao.close();
				
			}
			
		});
		
		getActivity().bindService(new Intent(getActivity(), PlaybackService.class), serverConn, Context.BIND_AUTO_CREATE);
		return rootView;
	}
	
	public void updateListActivity(Context context){
		this.getListAdapter();
		PlaylistDAO playlistDao;
		playlistDao = new PlaylistDAO(context);
		playlistDao.open();
		Cursor c = playlistDao.getPlaylist();
		PlaylistAdapter listAdapter = new PlaylistAdapter(context, c, 0);
		setListAdapter(listAdapter);
	}

	
	protected ServiceConnection serverConn = new ServiceConnection() {
		
		public void onServiceConnected(ComponentName className, IBinder binder){
			playbackService = ((PlaybackService.MyBinder) binder).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			playbackService = null;
			
		}
	};
	
	public void run() {
		if(playbackService != null){
			int currentPosition = 0;
			int total = playbackService.getMediaPlayerDuration();
			while(playbackService != null && currentPosition < total){
				try {
					Thread.sleep(1000);
					currentPosition = playbackService.getMediaPlayerCurrentPos();
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}
}
