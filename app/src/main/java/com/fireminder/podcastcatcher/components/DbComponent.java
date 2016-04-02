package com.fireminder.podcastcatcher.components;

import android.app.Activity;

import com.fireminder.podcastcatcher.mediaplayer.MediaPlayerService;
import com.fireminder.podcastcatcher.services.DownloadManagerService;
import com.fireminder.podcastcatcher.services.RetrieveEpisodeService;
import com.fireminder.podcastcatcher.ui.fragments.PodcastPlaybackFragment;
import com.fireminder.podcastcatcher.ui.fragments.PodcastsFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = { DbModule.class })
public interface DbComponent {
  void inject(Activity activity);
  void inject(MediaPlayerService service);
  void inject(RetrieveEpisodeService service);
  void inject(PodcastPlaybackFragment fragment);
  void inject(PodcastsFragment fragment);
  void inject(DownloadManagerService service);
}
