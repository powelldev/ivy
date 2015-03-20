package com.fireminder.podcastcatcher.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.drm.DrmStore;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.mediaplayer.MediaPlayerService;
import com.fireminder.podcastcatcher.models.Podcast;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract.Podcasts;
import com.fireminder.podcastcatcher.ui.activities.BaseActivity;
import com.fireminder.podcastcatcher.utils.PlaybackUtils;
import com.fireminder.podcastcatcher.utils.PrefUtils;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Responsible for displaying a list of all podcasts added to library.
 */
public class PodcastsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {


  private static final String LOG_TAG = PodcastsFragment.class.getSimpleName();
  PodcastAdapter mAdapter;
  private View mRoot = null;

  /**
   * Standard empty constructor
   */
  public PodcastsFragment() {
  }

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
    if (data.getCount() == 0) {
      // TODO: present an empty view prompting user to add Podcasts
    } else {
      PodcastAdapter adapter = new PodcastAdapter(getActivity(), data);
      getListView().setAdapter(adapter);
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) { /*Fragment closing, nothing to free*/ }

  private static class PodcastAdapter extends CursorAdapter implements
      View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private final Context mContext;
    private Podcast mPodcast;

    // Specifically need an activity here for dialog's context.
    public PodcastAdapter(Activity activity, Cursor cursor) {
      super(activity, cursor, 0);
      mContext = activity;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
      View view = LayoutInflater.from(context).inflate(R.layout.grid_item_podcast, parent, false);
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
      holder.image.setImageDrawable(TextDrawable.builder().buildRound(podcast.title.substring(0, 1), Color.BLUE));
      Picasso.with(mContext).load(podcast.imagePath).into(holder.image);
    }

    @Override
    public void onClick(View v) {

      // TODO figure out how run in backgroundthread
      String podcastId = (String) v.getTag(R.id.TAG_PODCAST_KEY);
      Cursor cursor = mContext.getContentResolver().query(
          Podcasts.buildPodcastUri(podcastId),
          null, null, null, null);
      cursor.moveToFirst();
      Podcast podcast = Podcast.parsePodcastFromCursor(cursor);
      switch (v.getId()) {
        case R.id.container:
        //TODO getNextEpisode will return null
        PlaybackUtils.downloadNextXEpisodes(mContext, podcast, 1);
        MediaPlayerService.playOrResumePodcast(mContext, podcast);
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
          new AlertDialog.Builder(mContext)
              .setTitle("Remove this podcast and all of its episodes?")
              .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    new DeletePodcastTask(mContext, mPodcast).execute();
                    Intent intent = new Intent(mContext, MediaPlayerService.class);
                    intent.setAction(MediaPlayerService.ACTION_STOP);
                    mContext.startService(intent);
                  }
                }
              )
              .setNegativeButton(android.R.string.cancel, null)
              .show();
          break;
        case R.id.download:
          int prefetch = PrefUtils.getNumEpisodesToPrefetch(mContext);
          PlaybackUtils.downloadNextXEpisodes(mContext, mPodcast, prefetch);
          break;
      }
      return false;
    }

    /* This was intended to let users reset an individual podcast so they could begin
    from the beginning. Users found it confusing - so instead we will allow access to individual
    episodes in a later patch.
     */
    private void resetPodcast() {
      // mark all episodes not listened to
      ContentValues cv = new ContentValues();
      cv.put(PodcastCatcherContract.Episodes.EPISODE_IS_COMPLETE, 0);
      mContext.getContentResolver().update(
          PodcastCatcherContract.Episodes.CONTENT_URI,
          cv,
          Podcasts.PODCAST_ID + "=?",
          new String[] {mPodcast.podcastId}
      );
      // if we're playing and we reset the playing podcast, stop and reset as well
      // TODO only stop playback if we reset a playing podcast
      Intent intent = new Intent(mContext, MediaPlayerService.class);
      intent.setAction(MediaPlayerService.ACTION_STOP);
      mContext.startService(intent);
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
        Cursor cursor = context.getContentResolver().query(
            PodcastCatcherContract.Episodes.CONTENT_URI,
            null,
            Podcasts.PODCAST_ID + "=?",
            new String[] {podcast.podcastId},
            null
        );
        while (cursor.moveToNext()) {
          Uri loc = Uri.parse(cursor.getString(cursor.getColumnIndex(PodcastCatcherContract.Episodes.EPISODE_LOCAL_URI)));
          new File(loc.getPath()).delete();
        }
        context.getContentResolver().delete(Podcasts.buildPodcastUri(podcast.podcastId), null, null);
        return null;
      }
    }
  }

}
