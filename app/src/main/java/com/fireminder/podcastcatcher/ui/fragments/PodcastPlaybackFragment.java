package com.fireminder.podcastcatcher.ui.fragments;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.mediaplayer.MediaPlayerControlView;
import com.fireminder.podcastcatcher.mediaplayer.MediaPlayerService;
import com.fireminder.podcastcatcher.models.Episode;
import com.squareup.picasso.Picasso;

/**
 *
 */
public class PodcastPlaybackFragment extends Fragment implements MediaPlayerControlView.Listener {


  public PodcastPlaybackFragment() {
  }

  private static final int NO_ARG = -1;

  private Handler mHandler = new Handler(new Handler.Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      switch (msg.what) {
        case MediaPlayerService.MSG_MEDIA_TITLE:
          String title = (String) msg.getData().get(MediaPlayerService.EXTRA_MEDIA_CONTENT);
          mediaPlayerControlView.setTitle(title);
          break;
        case MediaPlayerService.MSG_MEDIA_DURATION:
          mediaPlayerControlView.setDuration(msg.arg1);
          break;
        case MediaPlayerService.MSG_MEDIA_ELAPSED:
          mediaPlayerControlView.setProgress(msg.arg1);
          break;
        case MediaPlayerService.MSG_MEDIA_COMPLETE:
          mediaPlayerControlView.setProgress(0);
          break;
        case MediaPlayerService.MSG_HANDSHAKE_WITH_VIEW:
          Episode episode = (Episode) msg.getData().getParcelable(MediaPlayerService.EXTRA_MEDIA);
          String imageUri = msg.getData().getString(MediaPlayerService.EXTRA_MEDIA_CONTENT);
          setAlbumArt(imageUri);
          mEpisodeTitleTextView.setText(episode.title);
          mediaPlayerControlView.setProgress(msg.arg1);
          mediaPlayerControlView.setDuration(msg.arg2);
          break;
      }
      // All cases are handled
      return true;
    }
  });
  private final Messenger messengerToService = new Messenger(mHandler);
  private Messenger mService;

  private final ServiceConnection mConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      try {
        mService = new Messenger(service);
        Message msg = Message.obtain(null, MediaPlayerService.MSG_ADD_CLIENT);
        msg.replyTo = messengerToService;
        mService.send(msg);
        sendMessage(MediaPlayerService.MSG_HANDSHAKE_WITH_VIEW);
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      // unknown
    }
  };

  MediaPlayerControlView mediaPlayerControlView;
  ImageView mAlbumArtImageView;
  TextView mEpisodeTitleTextView;

  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_podcast_playback, container, false);
    mediaPlayerControlView = new MediaPlayerControlView(getActivity(), rootView);
    mediaPlayerControlView.setListener(this);
    mAlbumArtImageView = (ImageView) rootView.findViewById(R.id.episode_image);
    mEpisodeTitleTextView = (TextView) rootView.findViewById(R.id.episode_title);
    return rootView;
  }

  public void setAlbumArt(String imageUri) {
    Picasso.with(getActivity()).load(imageUri).into(mAlbumArtImageView);
  }

  @Override
  public void onResume() {
    super.onResume();
    Intent i = new Intent(getActivity(), MediaPlayerService.class);
    getActivity().startService(i);
    getActivity().bindService(new Intent(getActivity(), MediaPlayerService.class), mConnection, Context.BIND_ABOVE_CLIENT);
  }


  @Override
  public void onPlayPauseClicked() {
    sendMessage(MediaPlayerService.MSG_PLAY_PAUSE);
  }

  private void sendMessage(int what) {
    sendMessage(what, NO_ARG, null);
  }

  private void sendMessage(int what, int arg1) {
    sendMessage(what, arg1, null);
  }

  private void sendMessage(int what, int arg1, Bundle bundle) {
    try {
      Message msg = Message.obtain(null, what);
      msg.arg1 = arg1;
      if (bundle != null) {
        msg.setData(bundle);
      }
      mService.send(msg);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onRewindThirtyClicked() {
    sendMessage(MediaPlayerService.MSG_BACK_THIRTY, NO_ARG);
  }

  @Override
  public void onForwardThirtyClicked() {
    sendMessage(MediaPlayerService.MSG_FORWARD_THIRTY, NO_ARG);
  }

  @Override
  public void onSeekBarStarted() {
    sendMessage(MediaPlayerService.MSG_SEEK_START, NO_ARG);
  }

  @Override
  public void onSeekBarStopped(int progress) {
    sendMessage(MediaPlayerService.MSG_SEEK_END, progress);
  }

  @Override
  public void onSeekBarProgressChanged(int progress) {
    // Since seekBar changes are handled in onSeekBarStopped I don't see a reason to use this yet.
  }

  @Override
  public void onPreviousClicked() {
    sendMessage(MediaPlayerService.MSG_PREVIOUS);
  }

  @Override
  public void onNextClicked() {
    sendMessage(MediaPlayerService.MSG_NEXT);
  }

}

