package fireminder.podcastcatcher.fragments;

import android.app.ListFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import fireminder.podcastcatcher.PodcastCatcher;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.db.EpisodeDao;
import fireminder.podcastcatcher.ui.EpisodeAdapter;


public class RecentFragment extends ListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.channel, container, false);
        updateListAdapter();

        return rootView;
    }
    
    private void updateListAdapter(){
        Cursor cursor = new EpisodeDao().getAllRecentEpisodes();
        EpisodeAdapter cursorAdapter = new EpisodeAdapter(PodcastCatcher.getInstance().getContext(),
                cursor, 0);
        setListAdapter(cursorAdapter);
    }

}
