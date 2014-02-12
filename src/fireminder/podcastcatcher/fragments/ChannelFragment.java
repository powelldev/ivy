package fireminder.podcastcatcher.fragments;

import java.io.ByteArrayInputStream;
import java.io.File;

import android.app.ListFragment;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import fireminder.podcastcatcher.PodcastCatcher;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.activities.MainActivity;
import fireminder.podcastcatcher.db.EpisodeDao2;
import fireminder.podcastcatcher.db.PodcastDao2;
import fireminder.podcastcatcher.ui.EpisodeAdapter;
import fireminder.podcastcatcher.utils.Helper;
import fireminder.podcastcatcher.valueobjects.Episode;
import fireminder.podcastcatcher.valueobjects.Podcast;

public class ChannelFragment extends ListFragment implements
		OnItemClickListener {

	private final static String TAG = ChannelFragment.class.getSimpleName();

	EpisodeDao2 mEdao = new EpisodeDao2();
	PodcastDao2 mPdao = new PodcastDao2();

	public static ChannelFragment newInstance(long channelId) {
		ChannelFragment channelFragment = new ChannelFragment();
		Bundle args = new Bundle();
		args.putLong("channel_id", channelId);
		channelFragment.setArguments(args);
		return channelFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		long channelId;
		Podcast podcast;
		Bitmap image;

		View rootView = (View) inflater.inflate(R.layout.channel, container,
				false);

		channelId = getArguments().getLong("channel_id");

		podcast = mPdao.get(channelId);

		try {
			ByteArrayInputStream is = new ByteArrayInputStream(
					podcast.getImagePath());
			image = BitmapFactory.decodeStream(is);
		} catch (Exception e) {
			image = null;
		}

		((TextView) rootView.findViewById(R.id.title_tv)).setText(podcast
				.getTitle());
		if (image != null)
			((ImageView) rootView.findViewById(R.id.podcast_image))
					.setImageBitmap(image);
		else
			((ImageView) rootView.findViewById(R.id.podcast_image))
					.setImageResource(R.drawable.ic_launcher);

		return rootView;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		getListView().setOnItemClickListener(this);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		updateListAdapter(PodcastCatcher.getInstance().getContext());
		super.onViewCreated(view, savedInstanceState);
	}

	private void updateListAdapter(Context context) {
		Cursor cursor = mEdao.getAllEpisodesAsCursor(getArguments().getLong(
				"channel_id"));
		EpisodeAdapter cursorAdapter = new EpisodeAdapter(context, cursor, 0);
		setListAdapter(cursorAdapter);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
			long episode_id) {

		Episode episode = mEdao.get(episode_id);
		
		
		if (episode.getMp3().matches("")) {
			Helper.downloadEpisodeMp3(episode);
		} else {
			File mp3 = new File(episode.getMp3());
			if (mp3.exists()){
				MainActivity activity = (MainActivity) getActivity();
				activity.startPlayingEpisode(episode, mPdao.get(episode.getPodcast_id()));
				Toast.makeText(getActivity(), "Playing", Toast.LENGTH_SHORT).show();
			}
		}

	}

}
