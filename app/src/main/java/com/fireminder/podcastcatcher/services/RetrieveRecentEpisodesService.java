package com.fireminder.podcastcatcher.services;

import android.app.IntentService;
import android.content.Intent;


public class RetrieveRecentEpisodesService extends IntentService {
    private static final String LOG_TAG = RetrieveRecentEpisodesService.class.getSimpleName();

    public RetrieveRecentEpisodesService() {
        super("RetrieveEPisodeService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        // select * from episodes
        // order by pub date
        // limit 1
    }
}
