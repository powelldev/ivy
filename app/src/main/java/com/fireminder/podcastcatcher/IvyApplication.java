package com.fireminder.podcastcatcher;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.fireminder.podcastcatcher.components.DaggerDbComponent;
import com.fireminder.podcastcatcher.components.DbComponent;
import com.fireminder.podcastcatcher.components.DbModule;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(formKey="",
    mailTo = "thefireminder@gmail.com",
    mode = ReportingInteractionMode.TOAST,
    resToastText = R.string.crash_toast_text)
public class IvyApplication extends Application {

  private static IvyApplication sApplication;
  DbComponent dbComponent;

  public IvyApplication() {
    super();
  }

  @Override
  public void onCreate() {
    super.onCreate();
    sApplication = this;
    dbComponent = DaggerDbComponent.builder().dbModule(new DbModule()).build();
    ACRA.init(this);
  }

  public DbComponent getDbComponent() {
    return dbComponent;
  }

  public static IvyApplication getAppContext() {
    return sApplication;
  }

}
