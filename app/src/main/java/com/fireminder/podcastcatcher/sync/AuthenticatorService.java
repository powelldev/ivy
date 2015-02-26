package com.fireminder.podcastcatcher.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by michael on 2/19/2015.
 */
public class AuthenticatorService extends Service {

  private Authenticator mAuthenticator;

  @Override
  public void onCreate() {
    super.onCreate();
    mAuthenticator = new Authenticator(this);
  }

  @Override
  public IBinder onBind(Intent intent) {
    return mAuthenticator.getIBinder();
  }
}
