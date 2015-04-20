package com.fireminder.podcastcatcher;

import android.app.Application;

import com.facebook.stetho.Stetho;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(formKey="",
    mailTo = "thefireminder@gmail.com",
    mode = ReportingInteractionMode.TOAST,
    resToastText = R.string.crash_toast_text)
public class PodcastCatcherApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    ACRA.init(this);

  }
}
