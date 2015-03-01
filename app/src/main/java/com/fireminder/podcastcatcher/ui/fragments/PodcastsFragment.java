package com.fireminder.podcastcatcher.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.mediaplayer.MediaPlayerService;
import com.fireminder.podcastcatcher.models.Podcast;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract.Podcasts;
import com.fireminder.podcastcatcher.services.DownloadManagerService;
import com.fireminder.podcastcatcher.utils.PrefUtils;
import com.fireminder.podcastcatcher.utils.Utils;

/**
 * Responsible for displaying a list of all podcasts added to library.
 */
public class PodcastsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {


  private static final String LOG_TAG = PodcastsFragment.class.getSimpleName();
  PodcastAdapter mAdapter;
  private View mRoot = null;

  /***
   * Standard empty constructor
   */
  public PodcastsFragment() {}

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mRoot = inflater.inflate(R.layout.fragment_podcasts, container, false);
    return mRoot;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getLoaderManager().initLoader(0, null, this);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    // Simple query for all podcasts in db.
    return new CursorLoader(getActivity(), Podcasts.CONTENT_URI, null, null, null, null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    if (data.getCount() == 0 ) {
      // TODO: present an empty view prompting user to add Podcasts
    } else {
      PodcastAdapter adapter = new PodcastAdapter(getActivity(), data);
      getListView().setAdapter(adapter);
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) { /*Fragment closing, nothing to free*/ }

  /*
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
  */

  private static class PodcastAdapter extends CursorAdapter implements
      View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private Context mContext;
    private Podcast mPodcast;

    public PodcastAdapter(Context context, Cursor cursor) {
      super(context, cursor, 0);
      mContext = context.getApplicationContext();
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
      Podcast podcast = Podcast.parsePodcastFromCursor(cursor);
      ViewHolder holder = (ViewHolder) view.getTag();
      view.setTag(R.id.TAG_PODCAST_KEY, podcast.podcastId);
      view.setOnClickListener(this);
      holder.actions.setOnClickListener(this);
      holder.actions.setTag(R.id.TAG_PODCAST_KEY, podcast.podcastId);
      holder.title.setText(podcast.title);
      holder.description.setText(podcast.description);
      holder.timestamp.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
      String podcastId = (String) v.getTag(R.id.TAG_PODCAST_KEY);
      Cursor cursor = mContext.getContentResolver().query(
          Podcasts.buildPodcastUri(podcastId),
          null, null, null, null);
      cursor.moveToFirst();
      Podcast podcast = Podcast.parsePodcastFromCursor(cursor);
      switch (v.getId()) {
        case R.id.container:
          //TODO getNextEpisode will return null
          PrefUtils.setPodcastPlaying(mContext, podcast.podcastId);
          Intent intent = new Intent(mContext, MediaPlayerService.class);
          intent.setAction(MediaPlayerService.ACTION_PLAY);
          intent.putExtra(MediaPlayerService.EXTRA_MEDIA, Utils.getNextEpisode(mContext, podcast));
          mContext.startService(intent);
          break;
        case R.id.actions:
          mPodcast = podcast;
          PopupMenu popup = new PopupMenu(mContext, v);
          popup.getMenuInflater().inflate(R.menu.menu_podcasts, popup.getMenu());
          popup.setOnMenuItemClickListener(this);
          popup.show();
          break;
    }

  }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
      switch (menuItem.getItemId()) {
        case R.id.delete:
          new DeletePodcastTask(mContext, mPodcast).execute();
          break;
        case R.id.download:
          Utils.downloadAllEpisodes(mContext, mPodcast);
          break;
      }
      return false;
    }

    private static class ViewHolder {
      ImageView image;
      TextView title;
      TextView timestamp;
      TextView description;
      ImageButton actions;
    }


    private class DeletePodcastTask extends AsyncTask<Void, Void, Void> {

      private final Context context;
      private final Podcast podcast;

      public DeletePodcastTask(Context context, Podcast podcast) {
        this.context = context;
        this.podcast = podcast;
      }

      @Override
      protected Void doInBackground(Void... params) {
        context.getContentResolver().delete(Podcasts.buildPodcastUri(podcast.podcastId), null, null);
        return null;
      }
    }
  }

}
