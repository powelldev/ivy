package com.fireminder.podcastcatcher.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by michael on 2/19/2015.
 */
public class SyncService extends Service {
    private static EpisodeSyncAdapter sSyncAdapter = null;
    private static final Object sSyncAdapterLock = new Object();

    @Override
    public void onCreate() {
        super.onCreate();

        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new EpisodeSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
