package com.fireminder.podcastcatcher.ui.fragments;

import android.content.Context;
import android.database.Cursor;
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
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;
import com.fireminder.podcastcatcher.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ChannelFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

  private static final String LOG_TAG = ChannelFragment.class.getSimpleName();
  private static final String EXTRA_PODCAST_CHANNEL_ID = "podcast_channel_id";
  private static final String EXTRA_PODCAST_TITLE = "podcast_title";

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
    return new CursorLoader(getActivity(),
        PodcastCatcherContract.Episodes.CONTENT_URI,
        null,
        PodcastCatcherContract.Podcasts.PODCAST_ID + "=?",
        new String[]{podcastChannelId},
        null);

  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    if (cursor.getCount() == 0) {

    } else {
      EpisodeAdapter adapter = new EpisodeAdapter(getActivity(), cursor, podcastChannelId);
      getListView().setAdapter(adapter);
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
  }

  private static class EpisodeAdapter extends CursorAdapter implements
      View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private Context mContext;
    private String mPodcastChannelId;

    public EpisodeAdapter(Context context, Cursor cursor, String podcastChannelId) {
      super(context, cursor, 0);
      mContext = context.getApplicationContext();
      mPodcastChannelId = podcastChannelId;
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
      throw new UnsupportedOperationException("Not yet Implemented");

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
