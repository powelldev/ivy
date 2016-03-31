package com.fireminder.podcastcatcher.mediaplayer;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fireminder.podcastcatcher.R;

/**
 * View responsible for media player controls. Any layout must contain the button id's specified in initView.
 * Any Fragment must implement this classes Listener to recieve appropriate callbacks.
 */
public class MediaPlayerControlView implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

  TextView textViewElapsed;
  TextView textViewDuration;
  TextView textViewTitle;
  SeekBar seekbar;
  ImageButton mPlayPauseButton;

  Listener listener;

  Context mContext;

  public MediaPlayerControlView(Context context, View rootView) {
    mContext = context.getApplicationContext();
    initView(context, rootView);
  }

  private void initView(Context context, View rootView) {
    mPlayPauseButton = (ImageButton) rootView.findViewById(R.id.button_play_pause);
    mPlayPauseButton.setOnClickListener(this);
    rootView.findViewById(R.id.button_back_thirty).setOnClickListener(this);
    rootView.findViewById(R.id.button_forward_thirty).setOnClickListener(this);
    rootView.findViewById(R.id.button_next).setOnClickListener(this);
    rootView.findViewById(R.id.button_previous).setOnClickListener(this);
    textViewElapsed = (TextView) rootView.findViewById(R.id.text_view_elapsed);
    textViewDuration = (TextView) rootView.findViewById(R.id.text_view_duration);
    textViewTitle = (TextView) rootView.findViewById(R.id.text_view_title);
    seekbar = (SeekBar) rootView.findViewById(R.id.seek_bar);
    seekbar.setOnSeekBarChangeListener(this);
  }

  public void isPlaying(boolean isPlaying) {
    mPlayPauseButton.setImageResource(!isPlaying ? R.drawable.ic_play_arrow_black_48dp : R.drawable.ic_pause_black_48dp);
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

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.button_play_pause:
        listener.onPlayPauseClicked();
        break;
      case R.id.button_back_thirty:
        listener.onRewindThirtyClicked();
        break;
      case R.id.button_forward_thirty:
        listener.onForwardThirtyClicked();
        break;
      case R.id.button_next:
        listener.onNextClicked();
        break;
      case R.id.button_previous:
        listener.onPreviousClicked();
        break;
      default:
        throw new UnsupportedOperationException("Not yet implemented");
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
    int ss = millis / 1000 % 60;
    int mm = millis / 1000 / 60 % 60;
    if (millis < 1000 * 60 * 60) {
      return String.format("%02d:%02d", mm, ss);
    } else {
      int hh = ((millis / (1000*60*60)) % 24);
      return String.format("%d:%02d:%02d", hh, mm, ss);
    }
  }

  public interface Listener {
    void onPlayPauseClicked();
    void onRewindThirtyClicked();
    void onForwardThirtyClicked();
    void onSeekBarStarted();
    void onSeekBarStopped(int progress);
    void onSeekBarProgressChanged(int progress);
    void onPreviousClicked();
    void onNextClicked();
  }

}
