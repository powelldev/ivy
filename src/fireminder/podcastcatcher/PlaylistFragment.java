package fireminder.podcastcatcher;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import fireminder.podcastcatcher.db.Playlist;
import fireminder.podcastcatcher.db.PlaylistDAO;
import fireminder.podcastcatcher.ui.PlaylistAdapter;

public class PlaylistFragment extends ListFragment{

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.playlistfragment, container,
				false);
		updateListActivity(getActivity());
		ListView listview = (ListView) rootView.findViewById(android.R.id.list);
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long episode_id) {
				PlaylistDAO pdao = new PlaylistDAO(getActivity());
				pdao.open();
				Playlist playlist = new Playlist(pdao.getPlaylist());
				Toast.makeText(getActivity(),
						"" + playlist.episode_ids.get(arg2), Toast.LENGTH_LONG)
						.show();
				pdao.close();

			}

		});

		return rootView;
	}

	public void updateListActivity(Context context) {
		this.getListAdapter();
		PlaylistDAO playlistDao;
		playlistDao = new PlaylistDAO(context);
		playlistDao.open();
		Cursor c = playlistDao.getPlaylist();
		PlaylistAdapter listAdapter = new PlaylistAdapter(context, c, 0);
		setListAdapter(listAdapter);
	}

}
