package com.fireminder.podcastcatcher.ui.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;

public class RecentsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = RecentsFragment.class.getSimpleName();

    private SimpleCursorAdapter mAdapter;

    public RecentsFragment() {
    }

    public static RecentsFragment newInstance() {
        return new RecentsFragment();
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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                null,
                null,
                PodcastCatcherContract.Episodes.EPISODE_PUBLICATION_DATE + " DESC"
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

    public interface Listener {
        public void onFragmentInteraction(Uri uri);
    }

}
