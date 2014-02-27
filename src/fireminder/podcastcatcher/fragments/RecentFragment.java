package fireminder.podcastcatcher.fragments;

import java.io.File;

import android.app.ListFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.activities.MainActivity;
import fireminder.podcastcatcher.db.EpisodeDao;
import fireminder.podcastcatcher.db.PodcastDao;
import fireminder.podcastcatcher.ui.EpisodeAdapter;
import fireminder.podcastcatcher.ui.RecentAdapter;
import fireminder.podcastcatcher.utils.Helper;
import fireminder.podcastcatcher.valueobjects.Episode;


public class RecentFragment extends ListFragment implements OnItemClickListener {

    RecentAdapter mAdapter;

    public static RecentFragment newInstance() {
        return new RecentFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_recent, container, false);
        
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Cursor cursor = new EpisodeDao(getActivity()).getAllRecentEpisodes();
        mAdapter = new RecentAdapter(getActivity(), cursor, 0);
        setListAdapter(mAdapter);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
            long episode_id) {

        Episode episode = new EpisodeDao(getActivity()).get(episode_id);

        if (episode.getMp3().matches("")) {
            Helper.downloadEpisodeMp3(episode, getActivity());
        } else {
            File mp3 = new File(episode.getMp3());
            if (mp3.exists()) {
                MainActivity activity = (MainActivity) getActivity();
                activity.startPlayingEpisode(episode,
                        new PodcastDao(getActivity()).get(episode.getPodcast_id()));
                Toast.makeText(getActivity(), "Playing", Toast.LENGTH_SHORT)
                        .show();
            } else {
                Helper.downloadEpisodeMp3(episode, getActivity());
            }
        }

    }
    
    public void notifyDataSetChanged() {
        try { 
        ((EpisodeAdapter) getListAdapter()).notifyDataSetChanged();
        } catch (Exception e) {}
    }

}
       
