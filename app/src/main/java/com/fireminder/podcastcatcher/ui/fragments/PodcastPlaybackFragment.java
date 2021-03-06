package com.fireminder.podcastcatcher.ui.fragments;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.fireminder.podcastcatcher.IvyApplication;
import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.mediaplayer.MediaPlayerControlView;
import com.fireminder.podcastcatcher.mediaplayer.MediaPlayerService;
import com.fireminder.podcastcatcher.models.Episode;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;
import com.fireminder.podcastcatcher.utils.IvyPreferences;
import com.fireminder.podcastcatcher.utils.Logger;
import com.fireminder.podcastcatcher.utils.PlaybackUtils;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

public class PodcastPlaybackFragment extends Fragment implements MediaPlayerControlView.Listener {

  private static final String TAG = "PodcastPlaybackFragment";

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
        case MediaPlayerService.MSG_IS_PLAYING:
          mediaPlayerControlView.isPlaying(msg.arg1 == 1);
          break;
        case MediaPlayerService.MSG_HANDSHAKE_WITH_VIEW:
          Logger.i(TAG, "handshakingWithView");
          Episode episode = msg.getData().getParcelable(MediaPlayerService.EXTRA_MEDIA);
          String imageUri = msg.getData().getString(MediaPlayerService.EXTRA_MEDIA_CONTENT);
          Logger.i(TAG, "handshakingWithView: episode: " + episode.title);
          setAlbumArt(episode, imageUri);
          startAnim();
          mEpisodeTitleTextView.setText(episode.title);
          mediaPlayerControlView.setProgress(msg.arg1);
          mediaPlayerControlView.setDuration(msg.arg2);
          mediaPlayerControlView.isPlaying(true);
          break;
        case MediaPlayerService.MSG_NOTHING_PLAYING:
          mAlbumArtImageView.setImageResource(R.mipmap.ic_launcher);
          mEpisodeTitleTextView.setText("");
          mediaPlayerControlView.setProgress(0);
          mediaPlayerControlView.setDuration(0);
          mediaPlayerControlView.isPlaying(false);
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
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Inject
  IvyPreferences ivyPreferences;

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    String episodeId = ivyPreferences.getEpisodePlaying();
    Cursor cursor = getActivity().getContentResolver().query(PodcastCatcherContract.Episodes.buildEpisodeUri(episodeId), null, null, null, null);
    cursor.moveToFirst();
    if (cursor.getCount() > 0) {
      Episode episode = Episode.parseEpisodeFromCursor(cursor);
      String imageUri = PlaybackUtils.getEpisodeImage(getActivity(), episode);
      setAlbumArt(episode, imageUri);
      mEpisodeTitleTextView.setText(episode.title);
      mediaPlayerControlView.setProgress((int) episode.elapsed);
      mediaPlayerControlView.setDuration((int) episode.duration);
      mediaPlayerControlView.isPlaying(false);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    IvyApplication.getAppContext().getDbComponent().inject(this);

    View rootView = inflater.inflate(R.layout.fragment_podcast_playback, container, false);
    mediaPlayerControlView = new MediaPlayerControlView(getActivity(), rootView);
    mediaPlayerControlView.setListener(this);
    mAlbumArtImageView = (ImageView) rootView.findViewById(R.id.episode_image);
    mEpisodeTitleTextView = (TextView) rootView.findViewById(R.id.episode_title);

    return rootView;
  }

  public void getAlbumArt(Episode episode) {

  }

  public void setAlbumArt(Episode episode, String backupImage) {
    Logger.i(TAG, "setAlbumArt: " + episode.title + " backup: " + backupImage);
    try {
      // TODO load mp3 album cover art to separate file, using a byte array is causing OOM issues
      Picasso.with(getActivity()).load(backupImage).into(mAlbumArtImageView);
      /*
      if (mAlbumArtImageView.getTag() == episode.localUri) {
        // Already loaded
        return;
      }
      MediaMetadataRetriever retriever = new MediaMetadataRetriever();
      retriever.setDataSource(getActivity(), Uri.parse(episode.localUri));
      byte[] bArray = retriever.getEmbeddedPicture();
      if (bArray == null || bArray.length == 0) {
        Logger.d(TAG, "setAlbumArt, retriever failed to load bytes.");
        return;
      }
      Bitmap bm = BitmapUtil.decodeSampledBitmapFromByteArray(bArray,
          mAlbumArtImageView.getWidth(),
          mAlbumArtImageView.getHeight());
      mAlbumArtImageView.setImageBitmap(bm);
      mAlbumArtImageView.setTag(episode.localUri);
      if (bArray != null) {
        InputStream is = new ByteArrayInputStream(bArray);
        Bitmap bm = BitmapFactory.decodeStream(is);
        mAlbumArtImageView.setImageBitmap(bm);
      } else {
        Picasso.with(getActivity()).load(backupImage).into(mAlbumArtImageView);
      }
      */
    } catch (Exception e) {
      // Episode art DNE, consume
      e.printStackTrace();
    }
  }

  private void startAnim() {
    try {
      Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.animation);
      mAlbumArtImageView.startAnimation(animation);
    } catch (NullPointerException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    Intent i = new Intent(getActivity(), MediaPlayerService.class);
    getActivity().startService(i);
    getActivity().bindService(new Intent(getActivity(), MediaPlayerService.class), mConnection, Context.BIND_ABOVE_CLIENT);
  }

  @Override
  public void onPause() {
    super.onPause();
    getActivity().unbindService(mConnection);
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
    } catch (Exception e) {
      // Messenger not linked or not started yet.
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

