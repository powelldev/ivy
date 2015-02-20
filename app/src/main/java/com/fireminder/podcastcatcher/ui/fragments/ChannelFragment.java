package com.fireminder.podcastcatcher.ui.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;
import com.fireminder.podcastcatcher.utils.Logger;

public class ChannelFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {


    private static final String LOG_TAG = ChannelFragment.class.getSimpleName();
    private static final String EXTRA_PODCAST_CHANNEL_ID = "podcast_channel_id";
    private static final String EXTRA_PODCAST_TITLE = "podcast_title";

    private SimpleCursorAdapter mAdapter;

    private String podcastChannelId;

    public ChannelFragment() {
    }

    public static ChannelFragment newInstance(String podcastChannelId, String podcastTitle) {
        ChannelFragment fragment = new ChannelFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_PODCAST_CHANNEL_ID, podcastChannelId);
        args.putString(EXTRA_PODCAST_TITLE, podcastTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_2, null,
                new String[]{PodcastCatcherContract.Episodes.EPISODE_TITLE, PodcastCatcherContract.Episodes.EPISODE_DESCRIPTION},
                new int[]{android.R.id.text1, android.R.id.text2},
                0);
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
        registerForContextMenu(getListView());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        podcastChannelId = getArguments().getString(EXTRA_PODCAST_CHANNEL_ID);
        String podcastTitle = getArguments().getString(EXTRA_PODCAST_TITLE);
        getActivity().setTitle(podcastTitle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_channel, container, false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri episodeUri = PodcastCatcherContract.Episodes.CONTENT_URI;

        return new CursorLoader(getActivity(),
                episodeUri,
                null,
                PodcastCatcherContract.Podcasts.PODCAST_ID + "=?",
                new String[]{podcastChannelId},
                null
        );


    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_channel, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.menu_episode_delete:
                getActivity().getContentResolver().delete(PodcastCatcherContract.Episodes.CONTENT_URI,
                        BaseColumns._ID + "=?",
                        new String[]{Long.toString(info.id)});
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public interface Listener {
        public void onFragmentInteraction(Uri uri);
    }
}
