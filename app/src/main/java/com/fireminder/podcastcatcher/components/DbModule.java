package com.fireminder.podcastcatcher.components;

import com.fireminder.podcastcatcher.models.EpisodeModel;
import com.fireminder.podcastcatcher.net.DownloadManager;
import com.fireminder.podcastcatcher.net.FileManager;
import com.fireminder.podcastcatcher.net.HttpManager;
import com.fireminder.podcastcatcher.utils.IvyPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DbModule {

  @Provides
  @Singleton
  public EpisodeModel providesEpisodeModel() {
    return new EpisodeModel();
  }

  @Provides
  @Singleton
  public DownloadManager providesDownloadManager(FileManager fM, EpisodeModel eM, HttpManager hM) {
    return new DownloadManager(fM, eM, hM);
  }

  @Provides
  public FileManager providesFileManager() {
    return new FileManager();
  }

  @Provides
  public HttpManager providesHttpManager() {
    return new HttpManager();
  }

  @Provides
  @Singleton
  public IvyPreferences providesPreferences() {
    return new IvyPreferences();
  }
}
