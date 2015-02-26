package com.fireminder.podcastcatcher.ui.fragments;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.mediaplayer.MediaPlayerService;
import com.fireminder.podcastcatcher.models.Episode;
import com.fireminder.podcastcatcher.models.Playlist;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;
import com.fireminder.podcastcatcher.utils.Logger;
import com.mobeta.android.dslv.DragSortListView;

import java.util.List;

/**
 * Created by michael on 2/17/2015.
 */
public class PlaylistFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

  private static final String LOG_TAG = RecentsFragment.class.getSimpleName();

  private SimpleCursorAdapter mAdapter;

  public PlaylistFragment() {
  }

  public static PlaylistFragment newInstance() {
    return new PlaylistFragment();
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
    ((DragSortListView)getListView()).setDragSortListener(new DragSortListView.DragSortListener() {
      @Override
      public void drag(int from, int to) {
        Logger.d(LOG_TAG, "drag(from: " + from + ", to: " + to + " )");
      }

      @Override
      public void drop(int from, int to) {
        Logger.d(LOG_TAG, "drop(from: " + from + ", to: " + to + " )");
        if (from != to) {
          Cursor c = getActivity().getContentResolver().query(
              PodcastCatcherContract.Playlist.buildEpisodesDirUri(),
              null,
              null,
              null,
              PodcastCatcherContract.Playlist.PLAYLIST_ORDER + " ASC");
          Playlist playlist = Playlist.parsePlaylistFromCursor(c);
          Episode temp = playlist.episodes.get(from);
          playlist.episodes.set(from, playlist.episodes.get(to));
          playlist.episodes.set(to, temp);
          getActivity().getContentResolver().delete(PodcastCatcherContract.Playlist.CONTENT_URI, null, null);
          List<ContentValues> cvs = Playlist.playlistToContentValues(playlist);
          for (ContentValues values : cvs) {
            getActivity().getContentResolver().insert(PodcastCatcherContract.Playlist.CONTENT_URI, values);
          }
          mAdapter.notifyDataSetChanged();
        }
      }

      @Override
      public void remove(int which) {
        Logger.d(LOG_TAG, "remove( which: " + which + " )");
      }

    });
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_playlist, container, false);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    Uri playlistEpisodeUri = PodcastCatcherContract.Playlist.buildEpisodesDirUri();

    return new CursorLoader(getActivity(),
        playlistEpisodeUri,
        null,
        null,
        null,
        PodcastCatcherContract.Playlist.PLAYLIST_ORDER + " ASC");
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    Logger.e(LOG_TAG, "Playlist clicked");
    Cursor c = mAdapter.getCursor();
    c.moveToPosition(position);
    Episode e = Episode.parseEpisodeFromCursor(c);
    Intent i = new Intent(getActivity(), MediaPlayerService.class);
    i.setAction(MediaPlayerService.ACTION_PLAY);
    i.putExtra(MediaPlayerService.EXTRA_MEDIA, e);
    getActivity().startService(i);
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
