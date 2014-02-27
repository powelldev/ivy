package fireminder.podcastcatcher.fragments;

import java.io.File;

import android.app.ListFragment;
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

import com.squareup.picasso.Picasso;

import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.activities.MainActivity;
import fireminder.podcastcatcher.db.EpisodeDao;
import fireminder.podcastcatcher.db.PodcastDao;
import fireminder.podcastcatcher.ui.EpisodeAdapter;
import fireminder.podcastcatcher.utils.Helper;
import fireminder.podcastcatcher.valueobjects.Episode;
import fireminder.podcastcatcher.valueobjects.Podcast;

public class ChannelFragment extends ListFragment implements
        OnItemClickListener {

    private final static String TAG = ChannelFragment.class.getSimpleName();

    EpisodeAdapter cursorAdapter;

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

        PodcastDao mPdao = new PodcastDao(getActivity());

        podcast = mPdao.get(channelId);

        try {
            image = BitmapFactory.decodeFile(podcast.getImagePath());
        } catch (Exception e) {
            image = null;
        }

        ((TextView) rootView.findViewById(R.id.title_tv)).setText(podcast
                .getTitle());
        ImageView iv = (ImageView) rootView.findViewById(R.id.podcast_image);
        Picasso.with(getActivity()).load(podcast.getImagePath()).fit().centerCrop().placeholder(R.drawable.ic_launcher).noFade().into(iv);

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
        EpisodeDao mEdao = new EpisodeDao(getActivity());
        Cursor cursor = mEdao.getAllEpisodesAsCursorByDate(getArguments()
                .getLong("channel_id"));
        cursorAdapter = new EpisodeAdapter(getActivity(), cursor, 0);
        setListAdapter(cursorAdapter);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
            long episode_id) {

        PodcastDao mPdao = new PodcastDao(getActivity());
        EpisodeDao mEdao = new EpisodeDao(getActivity());
        Episode episode = mEdao.get(episode_id);

        if (episode.getMp3().matches("")) {
            Helper.downloadEpisodeMp3(episode, getActivity());
        } else {
            File mp3 = new File(episode.getMp3());
            if (mp3.exists()) {
                MainActivity activity = (MainActivity) getActivity();
                activity.startPlayingEpisode(episode,
                        mPdao.get(episode.getPodcast_id()));
                Toast.makeText(getActivity(), "Playing", Toast.LENGTH_SHORT)
                        .show();
            } else {
                Helper.downloadEpisodeMp3(episode, getActivity());
            }
        }

    }

    public void notifyDataSetChanged() {
        try { 
        ((EpisodeAdapter) getListAdapter()).notifyDataSetChanged();
        } catch (Exception e) {}
    }

}
