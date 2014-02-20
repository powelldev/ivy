package fireminder.podcastcatcher.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.services.PlaybackService;
import fireminder.podcastcatcher.ui.PodcastAdapter;
import fireminder.podcastcatcher.valueobjects.Episode;
import fireminder.podcastcatcher.valueobjects.Podcast;

public class PlayerLargeFragment extends Fragment implements OnClickListener,
        OnSeekBarChangeListener {

    public RelativeLayout header;
    private SeekBar mSeekBar;
    private TextView mElapsedTv;
    private TextView mMaxTv;

    private static class ViewHolder {
        static TextView episodeTitleTv;
        static TextView authorTitleTv;
        static ImageView albumCoverIv;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_player_large,
                container, false);

        header = (RelativeLayout) rootView
                .findViewById(R.id.fragment_player_header);

        ((ImageButton) rootView
                .findViewById(R.id.fragment_player_playpause_icon))
                .setOnClickListener(this);
        ((ImageButton) rootView.findViewById(R.id.fragment_player_rewind_icon))
                .setOnClickListener(this);
        ((ImageButton) rootView.findViewById(R.id.fragment_player_ff_icon))
                .setOnClickListener(this);

        mSeekBar = (SeekBar) rootView
                .findViewById(R.id.fragment_player_seek_bar);
        mSeekBar.setOnSeekBarChangeListener(this);

        mElapsedTv = (TextView) rootView
                .findViewById(R.id.fragment_player_text_elapsed);
        mMaxTv = (TextView) rootView
                .findViewById(R.id.fragment_player_text_max);

        ViewHolder.episodeTitleTv = (TextView) rootView
                .findViewById(R.id.fragment_player_small_title);
        ViewHolder.authorTitleTv = (TextView) rootView
                .findViewById(R.id.fragment_player_small_author);
        ViewHolder.albumCoverIv = (ImageView) rootView
                .findViewById(R.id.fragment_player_small_album_cover);

        return rootView;
    }

    private long mEpisodeId = -1;

    public void setEpisode(Episode episode, Podcast podcast) {
        mEpisodeId = episode.get_id();
        ViewHolder.episodeTitleTv.setText(episode.getTitle());
        ViewHolder.authorTitleTv.setText(episode.getDescription());
        Bitmap image = PodcastAdapter.getBitmapFromPodcast(podcast);
        if (image != null)
            ViewHolder.albumCoverIv.setImageBitmap(image);
        else
            ViewHolder.albumCoverIv.setImageResource(R.drawable.ic_launcher);
    }

    public long getCurrentEpisode() {
        return mEpisodeId;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), PlaybackService.class);
        switch (v.getId()) {
        case R.id.fragment_player_playpause_icon:
            intent.setAction("fireminder.PlaybackService.PLAY");
            getActivity().startService(intent);
            break;
        case R.id.fragment_player_rewind_icon:
            intent.setAction("fireminder.PlaybackService.REWIND");
            getActivity().startService(intent);
            break;
        case R.id.fragment_player_ff_icon:
            intent.setAction("fireminder.PlaybackService.FORWARD");
            getActivity().startService(intent);
            break;
        }
    }

    public void setMaxTime(int time) {
        mSeekBar.setMax(time);
        mMaxTv.setText("" + time);
    }

    public void updateTime(int time) {
        mSeekBar.setProgress(time);
        mElapsedTv.setText("" + time);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {
        if (fromUser) {
            Intent intent = new Intent(getActivity(), PlaybackService.class);
            intent.setAction("fireminder.PlaybackService.SEEK");
            intent.putExtra(PlaybackService.SEEK_EXTRA, progress);
            getActivity().startService(intent);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar arg0) {

    }

}
