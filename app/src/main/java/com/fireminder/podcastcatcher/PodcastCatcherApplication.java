package com.fireminder.podcastcatcher;

import android.app.Application;

import com.facebook.stetho.Stetho;

public class PodcastCatcherApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    Stetho.initialize(Stetho.newInitializerBuilder(this)
            .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
            .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
            .build()
    );

  }
}
