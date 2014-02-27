package fireminder.podcastcatcher.fragments;

import java.util.concurrent.TimeUnit;

import android.app.Fragment;
import android.content.Intent;
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

import com.squareup.picasso.Picasso;

import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.services.PlaybackService;
import fireminder.podcastcatcher.utils.Utils;
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
        static ImageView largeAlbumIv;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_player_large,
                container, false);

        header = (RelativeLayout) rootView
                .findViewById(R.id.fragment_player_header_with_info);

        ((ImageButton) rootView
                .findViewById(R.id.fragment_player_header_playpause_icon))
                .setOnClickListener(this);
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
        ViewHolder.largeAlbumIv = (ImageView) rootView
                .findViewById(R.id.fragment_player_large_album);
        return rootView;
    }

    private long mEpisodeId = -1;

    public void setEpisode(Episode episode, Podcast podcast) {
        mEpisodeId = episode.get_id();
        String title = episode.getTitle();
        if (title.length() > 40) {
            title = title.subSequence(0, 40 - 3) + "...";
        }
        ViewHolder.episodeTitleTv.setText(title);
        ViewHolder.authorTitleTv.setText("");
        Picasso.with(getActivity()).load(podcast.getImagePath()).fit()
                .centerCrop().noFade().placeholder(R.drawable.ic_launcher)
                .into(ViewHolder.albumCoverIv);

        Picasso.with(getActivity()).load(podcast.getImagePath()).fit()
                .centerCrop().noFade().placeholder(R.drawable.ic_launcher)
                .into(ViewHolder.largeAlbumIv);
    }

    public long getCurrentEpisode() {
        return mEpisodeId;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), PlaybackService.class);
        switch (v.getId()) {
        case R.id.fragment_player_header_playpause_icon:
        case R.id.fragment_player_playpause_icon:
            intent.setAction(PlaybackService.PLAY_PAUSE_ACTION);
            getActivity().startService(intent);
            break;
        case R.id.fragment_player_rewind_icon:
            intent.setAction(PlaybackService.REWIND_ACTION);
            getActivity().startService(intent);
            break;
        case R.id.fragment_player_ff_icon:
            intent.setAction(PlaybackService.FORWARD_ACTION);
            getActivity().startService(intent);
            break;
        }
    }

    public void setMaxTime(int time) {
        mSeekBar.setMax(time);
        mMaxTv.setText(convertMillisToHhmmss(time));
    }

    public String convertMillisToHhmmss(int time) {
        String hhmmss;
        if (time > 3600000) {
            hhmmss = String.format(
                    "%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(time),
                    TimeUnit.MILLISECONDS.toMinutes(time)
                            - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
                                    .toHours(time)), // The change is in this
                                                     // line
                    TimeUnit.MILLISECONDS.toSeconds(time)
                            - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                                    .toMinutes(time)));
        } else {
            hhmmss = String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(time)
                            - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
                                    .toHours(time)), // The change is in this
                                                     // line
                    TimeUnit.MILLISECONDS.toSeconds(time)
                            - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                                    .toMinutes(time)));
        }
        return hhmmss;
    }

    public void updateTime(int time) {
        mSeekBar.setProgress(time);
        mElapsedTv.setText(convertMillisToHhmmss(time));
    }

    public void setHeaderVisible(boolean visible) {
        if (visible) {
            getView().findViewById(R.id.fragment_player_header_playpause_icon)
                    .setVisibility(View.VISIBLE);
        } else {
            getView().findViewById(R.id.fragment_player_header_playpause_icon)
                    .setVisibility(View.GONE);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {
        if (fromUser) {
            Log.e(Utils.TAG, "Starting seek from user: " + progress);
            Intent intent = new Intent(getActivity(), PlaybackService.class);
            intent.setAction(PlaybackService.SEEK_ACTION);
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

    public void setPlayOn(boolean playing) {
        if (playing) {
            ((ImageButton) getView().findViewById(
                    R.id.fragment_player_header_playpause_icon))
                    .setActivated(true);
            ((ImageButton) getView().findViewById(
                    R.id.fragment_player_playpause_icon)).setActivated(false);
        } else {
            ((ImageButton) getView().findViewById(
                    R.id.fragment_player_header_playpause_icon))
                    .setActivated(false);
            ((ImageButton) getView().findViewById(
                    R.id.fragment_player_playpause_icon)).setActivated(true);

        }
    }
}
