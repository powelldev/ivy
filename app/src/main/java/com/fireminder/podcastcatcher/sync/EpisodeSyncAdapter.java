package com.fireminder.podcastcatcher.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;

import com.fireminder.podcastcatcher.models.Episode;
import com.fireminder.podcastcatcher.models.Podcast;
import com.fireminder.podcastcatcher.models.PodcastHandler;
import com.fireminder.podcastcatcher.provider.PodcastCatcherContract;
import com.fireminder.podcastcatcher.utils.Logger;
import com.fireminder.podcastcatcher.utils.RssParseHelper;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

/**
 * Created by michael on 2/19/2015.
 */
public class EpisodeSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String LOG_TAG = EpisodeSyncAdapter.class.getSimpleName();

    private Context mContext;

    public EpisodeSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context.getApplicationContext();

    }

    @Override
    public void onPerformSync(final Account account, Bundle extras, String authority,
                              final ContentProviderClient provider, final SyncResult syncResult) {

        Logger.e(LOG_TAG, "Starting service");
        Cursor podcastCursor = mContext.getApplicationContext().getContentResolver()
                .query(PodcastCatcherContract.Podcasts.CONTENT_URI,
                        null,
                        null,
                        null,
                        null);

        while (podcastCursor.moveToNext()) {
            Podcast podcast = PodcastHandler.parsePodcastFromCursor(podcastCursor);
            Cursor episodeCursor = mContext.getApplicationContext().getContentResolver()
                    .query(PodcastCatcherContract.Episodes.CONTENT_URI,
                            null,
                            PodcastCatcherContract.Podcasts.PODCAST_ID + "=?",
                            new String[]{podcast.podcastId},
                            PodcastCatcherContract.Episodes.EPISODE_PUBLICATION_DATE + " DESC LIMIT 1");
            episodeCursor.moveToFirst();
            long pubDate = episodeCursor.getLong(episodeCursor.getColumnIndex(PodcastCatcherContract.Episodes.EPISODE_PUBLICATION_DATE));
            PrettyTime p = new PrettyTime();
            Logger.e(LOG_TAG, "Podcast: " + podcast.title + " last pub date: " + p.format(new Date(pubDate)));
            try {
                URL url = new URL(podcast.feed);
                InputStream is = url.openConnection().getInputStream();
                List<Episode> episodes = RssParseHelper.parseNewEpisodesFromXml(is, podcast.podcastId, pubDate);
                for (Episode episode : episodes)
                    Logger.e(LOG_TAG, "Episode : " + episode.title + " parsed.");
            } catch (Exception e) {
                Logger.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }

        }
    }
}
