package com.fireminder.podcastcatcher.components;

import com.fireminder.podcastcatcher.models.EpisodeModel;
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
  public IvyPreferences providesPreferences() {
    return new IvyPreferences();
  }
}
