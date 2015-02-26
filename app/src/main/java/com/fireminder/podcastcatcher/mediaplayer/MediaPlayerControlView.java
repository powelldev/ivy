package com.fireminder.podcastcatcher.mediaplayer;

import android.content.Context;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fireminder.podcastcatcher.R;

/**
 * Created by mpowell on 2/21/15.
 */
public class MediaPlayerControlView implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

  TextView textViewElapsed;
  TextView textViewDuration;
  TextView textViewTitle;
  SeekBar seekbar;

  Listener listener;

  Context mContext;

  public MediaPlayerControlView(Context context, View rootView) {
    mContext = context.getApplicationContext();
    initView(context, rootView);
  }

  private void initView(Context context, View rootView) {
    rootView.findViewById(R.id.button_play_pause).setOnClickListener(this);
    rootView.findViewById(R.id.button_set_data).setOnClickListener(this);
    rootView.findViewById(R.id.button_back_thirty).setOnClickListener(this);
    rootView.findViewById(R.id.button_forward_thirty).setOnClickListener(this);
    rootView.findViewById(R.id.button_set_playlist).setOnClickListener(this);
    textViewElapsed = (TextView) rootView.findViewById(R.id.text_view_elapsed);
    textViewDuration = (TextView) rootView.findViewById(R.id.text_view_duration);
    textViewTitle = (TextView) rootView.findViewById(R.id.text_view_title);
    seekbar = (SeekBar) rootView.findViewById(R.id.seek_bar);
    seekbar.setOnSeekBarChangeListener(this);
  }

  public void setListener(Listener listener) {
    this.listener = listener;
  }

  public void setDuration(int duration) {
    seekbar.setMax(duration);
    textViewDuration.setText(millisToTimeView(duration));
  }

  public void setProgress(int progress) {
    seekbar.setProgress(progress);
    textViewElapsed.setText(millisToTimeView(progress));
  }

  public void setTitle(String title) {
    textViewTitle.setText(title);
  }

  public interface Listener {
    public void onPlayPauseClicked();

    public void onSetDataClicked();

    public void onRewindThirtyClicked();

    public void onForwardThirtyClicked();

    public void onSetPlaylist();

    public void onSeekBarStarted();

    public void onSeekBarStopped(int progress);

    public void onSeekBarProgressChanged(int progress);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.button_play_pause:
        listener.onPlayPauseClicked();
        break;
      case R.id.button_set_data:
        listener.onSetDataClicked();
        break;
      case R.id.button_back_thirty:
        listener.onRewindThirtyClicked();
        break;
      case R.id.button_forward_thirty:
        listener.onForwardThirtyClicked();
        break;
      case R.id.button_set_playlist:
        listener.onSetPlaylist();
        break;
    }

  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    textViewElapsed.setText(millisToTimeView(progress));
    listener.onSeekBarProgressChanged(progress);
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
    listener.onSeekBarStarted();

  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
    listener.onSeekBarStopped(seekBar.getProgress());

  }


  private static String millisToTimeView(int millis) {
    // I don't desire the overhead TimeUtils or Date formatting would cause
    // and I don't expect this arithmetic to produce bugs often.
    int ss = millis / 1000 % 60;
    int mm = millis / 1000 / 60 % 60;
    if (millis < 1000 * 60 * 60) {
      return String.format("%02d:%02d", mm, ss);
    } else {
      int hh = millis / 1000 / 360 % 60;
      return String.format("%d:%02d:%02d", hh, mm, ss);
    }
  }
}
