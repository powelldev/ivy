package com.fireminder.podcastcatcher.ui.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.models.Podcast;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract.Podcasts;

import static com.fireminder.podcastcatcher.models.Podcast.parsePodcastFromCursor;

public class PodcastsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {


  private static final String LOG_TAG = PodcastsFragment.class.getSimpleName();
  SimpleCursorAdapter mAdapter;
  private View mRoot = null;

  public PodcastsFragment() {
  }

  public static PodcastsFragment newInstance() {
    return new PodcastsFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mRoot = inflater.inflate(R.layout.fragment_podcasts, container, false);
    return mRoot;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    mAdapter = new SimpleCursorAdapter(getActivity(),
        android.R.layout.simple_list_item_2, null,
        new String[]{Podcasts.PODCAST_TITLE, Podcasts.PODCAST_DESCRIPTION},
        new int[]{android.R.id.text1, android.R.id.text2},
        0);
    setListAdapter(mAdapter);
    registerForContextMenu(getListView());
    getLoaderManager().initLoader(0, null, this);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);
    Cursor c = mAdapter.getCursor();
    c.moveToPosition(position);
    Podcast p = parsePodcastFromCursor(c);

    if (getActivity() instanceof Listener) {
      ((Listener) getActivity()).onPodcastItemInteraction(p);
    }
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    Uri baseUri = Podcasts.CONTENT_URI;
    return new CursorLoader(getActivity(), baseUri, null, null, null, null);
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
    inflater.inflate(R.menu.menu_podcasts, menu);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    switch (item.getItemId()) {
      case R.id.menu_podcasts_delete:
        getActivity().getContentResolver().delete(Podcasts.CONTENT_URI,
            BaseColumns._ID + "=?",
            new String[]{Long.toString(info.id)});
        return true;
      default:
        return super.onContextItemSelected(item);
    }
  }

  public interface Listener {
    public void onPodcastItemInteraction(Podcast podcast);
  }

}
