package com.fireminder.podcastcatcher.ui.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.CursorAdapter;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.models.Episode;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;

/**
 * Created by powelldev on 2/22/15.
 */
public abstract class EpisodeFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /*
  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    Cursor c = mAdapter.getCursor();
    c.moveToPosition(position);
    Episode e = EpisodeHandler.parseEpisodeFromCursor(c);
    Intent i = new Intent(getActivity(), DownloadManagerService.class);
    i.setAction(DownloadManagerService.ACTION_START_DOWNLOAD);
    i.putExtra(DownloadManagerService.EXTRA_EPISODE, e);
    getActivity().startService(i);
    ContentValues values = new ContentValues();
    values.put(PodcastCatcherContract.Playlist.PLAYLIST_ORDER, position);
    values.put(PodcastCatcherContract.Episodes.EPISODE_ID, e.episode_id);
    getActivity().getContentResolver().insert(PodcastCatcherContract.Playlist.CONTENT_URI, values);
  }
    */

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getLoaderManager().initLoader(0, null, this);
    registerForContextMenu(getListView());
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_channel, container, false);
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

  private static class EpisodeAdapter extends CursorAdapter implements
      View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    public EpisodeAdapter(Context context, Cursor cursor) {
      super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
      View view = LayoutInflater.from(context).inflate(R.layout.list_item_people, parent, false);
      ViewHolder holder = new ViewHolder();
      holder.timestamp = (TextView) view.findViewById(R.id.timestamp);
      holder.description = (TextView) view.findViewById(R.id.description);
      holder.title = (TextView) view.findViewById(R.id.title);
      holder.image = (ImageView) view.findViewById(R.id.image);
      holder.actions = (ImageButton) view.findViewById(R.id.actions);
      view.setTag(holder);
      return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
      Episode episode = Episode.parseEpisodeFromCursor(cursor);
      ViewHolder holder = (ViewHolder) view.getTag();
      view.setOnClickListener(this);
      holder.actions.setOnClickListener(this);
      holder.title.setText(episode.title);
      holder.description.setText(episode.description);
      holder.timestamp.setText("" + episode.pubDate);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
      return false;
    }

    private static class ViewHolder {
      ImageView image;
      TextView title;
      TextView timestamp;
      TextView description;
      ImageButton actions;
    }

  }
}
