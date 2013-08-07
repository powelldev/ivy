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
import fireminder.podcastcatcher.db.EpisodeDAO;
import fireminder.podcastcatcher.ui.EpisodeAdapter;

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
			}
		});
		return rootView;
	}

	public void updateListActivity(Context context) {
		Playlist playlist = Playlist.instance;
		EpisodeDAO episodeDao = new EpisodeDAO(context);
		episodeDao.open();
		//EpisodeAdapter cursorAdapter = new EpisodeAdapter(getActivity(), playlistCursor, 0);
		//setListAdapter(cursorAdapter);
		this.getListAdapter();
	}

}
