package fireminder.podcastcatcher.services;

import java.io.File;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.StatefulMediaPlayer;
import fireminder.podcastcatcher.activities.MainActivity;
import fireminder.podcastcatcher.db.EpisodeDao;
import fireminder.podcastcatcher.valueobjects.Episode;

public class PlaybackService extends Service {

    private StatefulMediaPlayer mPlayer;
    public static final String EPISODE_EXTRA = "episode_extra";
    private static final String TAG = PlaybackService.class.getSimpleName();
    private EpisodeDao mEdao = new EpisodeDao();;
    private long mEpisodeId;
    private int mElapsed;

    @Override
    public void onCreate() {
        mPlayer = new StatefulMediaPlayer();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try { 
        if (intent.getAction().contains("START")) {
            start(intent);
        } else if (intent.getAction().contains("PLAY")) {
            play();
        } else if (intent.getAction().contains("REWIND")) {
            this.setForeground(false);
            mPlayer.pause();
            //mHandler.removeCallbacks(updateProgressRunnable);
            mHandler.removeCallbacksAndMessages(null);
            Episode episode = mEdao.get(mEpisodeId);
            episode.setElapsed(mElapsed);
            mEdao.update(episode);
            Log.e(TAG, "Elapsed update: " + episode.getElapsed());
        }
        } catch (Exception e) {
            Log.e(TAG, "Err in onStartCommand: " + e.getMessage());
        }
        return Service.START_STICKY;
    }

    private void play() {
        setForeground(true);
        mHandler.post(updateProgressRunnable);
        mPlayer.start();
    }
    private void start(Intent intent) {
            mEpisodeId = intent.getExtras().getLong(EPISODE_EXTRA);
        if (mPlayer.getPlayingEpisodeId() != mEpisodeId) {
            Episode episode = mEdao.get(mEpisodeId);
            setForeground(true);
            Log.e(TAG, episode.getMp3());
            File file = new File(episode.getMp3());
            Log.e(TAG, file.getAbsolutePath());
            if (file.exists()) {
                mPlayer.setDataSource(episode);
                mPlayer.startAt(episode.getElapsed());
                mHandler.post(updateProgressRunnable);
            }
        } 
    }

    private void setForeground(boolean on) {
        if (on) {
            Notification notification = new Notification(
                    R.drawable.ic_launcher, getText(R.string.app_name),
                    System.currentTimeMillis());
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);
            notification.setLatestEventInfo(this, "Title", "Text",
                    pendingIntent);
            startForeground(42, notification);
        } else {
            stopForeground(true);
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    Handler mHandler = new Handler();
    Runnable updateProgressRunnable = new Runnable() {

        @Override
        public void run() {
            Log.e(TAG, mPlayer.getCurrentTrack());
            Log.e(TAG, "" + mPlayer.getCurrentPosition());
            mElapsed = mPlayer.getCurrentPosition();
            mHandler.postDelayed(this, 1000);
        }

    };
}
