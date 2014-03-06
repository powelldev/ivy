package fireminder.podcastcatcher.fragments;

import java.io.File;

import android.app.ListFragment;
import android.content.ClipData;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.activities.MainActivity;
import fireminder.podcastcatcher.db.EpisodeDao;
import fireminder.podcastcatcher.db.PodcastDao;
import fireminder.podcastcatcher.ui.EpisodeAdapter;
import fireminder.podcastcatcher.ui.PlaylistAdapter;
import fireminder.podcastcatcher.ui.RecentAdapter;
import fireminder.podcastcatcher.utils.Helper;
import fireminder.podcastcatcher.utils.Utils;
import fireminder.podcastcatcher.valueobjects.Episode;

public class PlaylistFragment extends ListFragment implements
        OnItemClickListener, OnItemLongClickListener, OnDragListener {

    PlaylistAdapter mAdapter;

    public static PlaylistFragment newInstance() {
        return new PlaylistFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_recent, container,
                false);

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle("Playlist");
        getListView().setOnItemClickListener(this);
        getListView().setOnItemLongClickListener(this);
        getListView().setOnDragListener(this);
        setListAdapter(mAdapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Cursor cursor = new EpisodeDao(getActivity())
                .getPlaylistEpisodesAsCursor();
        getListView().setOnItemClickListener(this);
        mAdapter = new PlaylistAdapter(getActivity(), cursor, 0);
        setListAdapter(mAdapter);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
            long episode_id) {

        Toast.makeText(getActivity(), "Playing", Toast.LENGTH_SHORT).show();
        Episode episode = new EpisodeDao(getActivity()).get(episode_id);

        if (episode.getMp3().matches("")) {
            Helper.downloadEpisodeMp3(episode, getActivity());
        } else {
            File mp3 = new File(episode.getMp3());
            if (mp3.exists()) {
                MainActivity activity = (MainActivity) getActivity();
                activity.startPlayingEpisode(episode, new PodcastDao(
                        getActivity()).get(episode.getPodcast_id()));
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
        } catch (Exception e) {
        }
    }

    @Override
    public boolean onDrag(View view, DragEvent event) {
        long itemOne = 0, itemTwo;
        switch (event.getAction()) {
        case DragEvent.ACTION_DRAG_STARTED:
            itemOne = getPosition(event);
            Log.e(Utils.TAG, "Item One: " + itemOne);
            return true;
        case DragEvent.ACTION_DROP:
            itemTwo = getPosition(event);
            Log.e(Utils.TAG, "Item Two: " + itemTwo);
            return true;
        }
        return true;
    }

    private long getPosition(DragEvent event) {
        Log.e(Utils.TAG, "X: " + event.getX());
        Log.e(Utils.TAG, "Y: " + event.getY());
        int x = (int) Math.ceil(event.getX());
        int y = (int) Math.ceil(event.getY());
        long pos = getListView().pointToRowId(x, y);
        if (pos == ListView.INVALID_ROW_ID) {
            pos = getListView().getItemIdAtPosition(getListView().getCount()-1);
        }
        return pos;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View view, int arg2,
            long arg3) {
        ClipData data = ClipData.newPlainText((CharSequence) view.getTag(),
                "text");
        View.DragShadowBuilder builder = new View.DragShadowBuilder(view);
        view.startDrag(data, builder, null, 0);
        return false;
    }

}
