package com.fireminder.podcastcatcher.ui.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.models.Episode;
import com.fireminder.podcastcatcher.models.Playlist;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;
import com.fireminder.podcastcatcher.services.DownloadManagerService;
import com.fireminder.podcastcatcher.utils.Logger;
import com.fireminder.podcastcatcher.utils.Utils;

public class RecentsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

  private static final String LOG_TAG = RecentsFragment.class.getSimpleName();

  public RecentsFragment() {
  }

  public static RecentsFragment newInstance() {
    return new RecentsFragment();
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
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
  public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    if (cursor.getCount() == 0) {

    } else {
      EpisodeAdapter adapter = new EpisodeAdapter(getActivity(), cursor);
      getListView().setAdapter(adapter);
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    try {
      ((CursorAdapter) getListView().getAdapter()).notifyDataSetChanged();
    } catch (IllegalStateException e) {
      // Can be thrown when changing activities
      // Consume
      e.printStackTrace();
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
      holder.downloadedIcon = (ImageView) view.findViewById(R.id.downloaded_image);
      holder.actions = (ImageButton) view.findViewById(R.id.actions);
      view.setTag(holder);
      return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
      Episode episode = Episode.parseEpisodeFromCursor(cursor);
      view.setTag(R.id.TAG_EPISODE_KEY, episode.episode_id);
      ViewHolder holder = (ViewHolder) view.getTag();
      view.setOnClickListener(this);
      holder.actions.setOnClickListener(this);
      holder.actions.setTag(R.id.TAG_EPISODE_KEY, episode.episode_id);
      holder.title.setText(episode.title);
      holder.description.setText(episode.description);
      holder.timestamp.setText(Utils.makeTimePretty(episode.pubDate));
      if (episode.isDownloaded) {
        holder.downloadedIcon.setVisibility(View.VISIBLE);
      } else {
        holder.downloadedIcon.setVisibility(View.INVISIBLE);
      }
    }

    @Override
    public void onClick(View v) {
      String id = (String) v.getTag(R.id.TAG_EPISODE_KEY);
      Cursor cursor = mContext.getContentResolver().query(PodcastCatcherContract.Episodes.buildEpisodeUri(id),
          null, null, null, null);
      cursor.moveToFirst();
      Episode episode = Episode.parseEpisodeFromCursor(cursor);
      switch (v.getId()) {
        case R.id.container:
          if (!episode.isDownloaded) {
            DownloadManagerService.download(mContext.getApplicationContext(), episode);
          } else {
            enqueue(episode);
          }
          break;
        case R.id.actions:
          // onMenuItemClick is a member method, set mEpisode as a means of
          // passing an episode to it.
          mEpisode = episode;
          PopupMenu popup = new PopupMenu(mContext, v);
          popup.getMenuInflater().inflate(R.menu.menu_recents, popup.getMenu());
          popup.setOnMenuItemClickListener(this);
          popup.show();
          break;
      }
    }

    private Episode mEpisode;

    private void enqueue(Episode episode) {
      Logger.e(LOG_TAG, "Enqueuing: " + episode.title);

      if (!episode.isDownloaded) {
        Intent i = new Intent(mContext, DownloadManagerService.class);
        i.setAction(DownloadManagerService.ACTION_START_DOWNLOAD);
        i.putExtra(DownloadManagerService.EXTRA_EPISODE, episode);
        mContext.startService(i);
      }

      ContentValues values = new ContentValues();
      values.put(PodcastCatcherContract.Playlist.PLAYLIST_ORDER, Playlist.getItemCount(mContext));
      values.put(PodcastCatcherContract.Episodes.EPISODE_ID, episode.episode_id);
      mContext.getContentResolver().insert(PodcastCatcherContract.Playlist.CONTENT_URI, values);

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
      throw new UnsupportedOperationException("Not yet implemented");
    }

    private static class ViewHolder {
      ImageView image;
      ImageView downloadedIcon;
      TextView title;
      TextView timestamp;
      TextView description;
      ImageButton actions;
    }

  }
}




