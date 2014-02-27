package fireminder.podcastcatcher.services;

import java.io.File;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

import fireminder.podcastcatcher.LockscreenManager;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.StatefulMediaPlayer;
import fireminder.podcastcatcher.activities.MainActivity;
import fireminder.podcastcatcher.db.EpisodeDao;
import fireminder.podcastcatcher.db.PodcastDao;
import fireminder.podcastcatcher.utils.Utils;
import fireminder.podcastcatcher.valueobjects.Episode;
import fireminder.podcastcatcher.valueobjects.Podcast;

public class PlaybackService extends Service implements Target {

    private static final String TAG = PlaybackService.class.getSimpleName();

    public static final String MAX_EXTRA = "max";
    public static final String SEEK_EXTRA = "seek";
    public static final String TIME_EXTRA = "timing";
    public static final String EPISODE_EXTRA = "episode";

    public static final String MAX_INTENT = "fireminder.podcastcatcher.services.PlaybackService.TIME_MAX";
    public static final String TIME_INTENT = "fireminder.podcastcatcher.services.PlaybackService.TIME_UPDATE";

    public static final String START_ACTION = "fireminder.podcastcatcher.services.PlaybackService.START";
    public static final String SET_ACTION = "fireminder.podcastcatcher.services.PlaybackService.SET";
    public static final String PLAY_PAUSE_ACTION = "fireminder.podcastcatcher.services.PlaybackService.PLAY_PAUSE";
    public static final String FORWARD_ACTION = "fireminder.podcastcatcher.services.PlaybackService.FORWARD";
    public static final String REWIND_ACTION = "fireminder.podcastcatcher.services.PlaybackService.REWIND";
    public static final String SEEK_ACTION = "fireminder.podcastcatcher.services.PlaybackService.SEEK";
    public static final String FOREGROUND_ON_ACTION = "fireminder.podcastcatcher.services.PlaybackService.FOREGROUND_ON";
    public static final String FOREGROUND_OFF_ACTION = "fireminder.podcastcatcher.services.PlaybackService.FOREGROUND_OFF";

    private long mEpisodeId;
    private int mElapsed;
    private StatefulMediaPlayer mPlayer;
    private LocalBroadcastManager mBroadcaster;
    private LockscreenManager mLockscreen;

    @Override
    public void onCreate() {
        mPlayer = new StatefulMediaPlayer();
        mBroadcaster = LocalBroadcastManager
                .getInstance(getApplicationContext());

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        this.registerReceiver(mReceiver, intentFilter);

        mLockscreen = new LockscreenManager(getApplicationContext());
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        mPlayer.release();
        mPlayer = null;
        this.unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action == START_ACTION) {
            start(intent);
        } else if (action == SET_ACTION) {
            set(intent);
        } else if (action == PLAY_PAUSE_ACTION) {
            Log.e(TAG, intent.getAction() + mPlayer.isPlaying());
            if (mPlayer.isPlaying()) {
                pause();
            } else {
                Log.e(TAG, "Playing...");
                play();
            }
        } else if (action == FORWARD_ACTION) {
            Log.e(Utils.TAG, intent.getAction());
            boolean player = mPlayer.isPlaying();
            int i = mPlayer.getCurrentPosition() + 30000;
            mPlayer.pause();
            mPlayer.seek(i);
            if (player)
                mPlayer.start();
        } else if (action == REWIND_ACTION) {
            Log.e(Utils.TAG, intent.getAction());
            boolean player = mPlayer.isPlaying();
            int i = mPlayer.getCurrentPosition() - 30000;
            if (i < 0) {
                i = 0;
            }
            mPlayer.pause();
            mPlayer.seek(i);
            if (player)
                mPlayer.start();
        } else if (action == SEEK_ACTION) {
            int time = intent.getIntExtra(SEEK_EXTRA, 0);
            Log.e(Utils.TAG, "SEEKING PROGRESS INTENT: " + time);
            mPlayer.seek(time);
        } else if (action == FOREGROUND_ON_ACTION) {
            if (mPlayer.isPlaying()) {
                setForeground(true);
            }
        } else if (action == FOREGROUND_OFF_ACTION) {
            setForeground(false);
        }
        return Service.START_STICKY;
    }

    private void stop() {
        mLockscreen.removeLockscreenControls(getApplicationContext());
        this.stopSelf();
    }

    private void pause() {
        mPlayer.pause();
        mHandler.removeCallbacksAndMessages(null);
        EpisodeDao mEdao = new EpisodeDao(getApplicationContext());
        Episode episode = mEdao.get(mEpisodeId);
        episode.setElapsed(mElapsed);
        mEdao.update(episode);
        Log.e(TAG, "Elapsed update: " + episode.getElapsed());
        mLockscreen.setLockscreenPaused();
    }

    private void play() {
        mHandler.post(updateProgressRunnable);
        EpisodeDao mEdao = new EpisodeDao(getApplicationContext());
        Episode episode = mEdao.get(mEpisodeId);
        sentEpisodeMax(episode);
        Log.e(Utils.TAG, "Requesting audio focus");
        Episode e = mEdao.get(mEpisodeId);
        Podcast p = new PodcastDao(getApplicationContext()).get(e
                .getPodcast_id());
        mLockscreen.requestAudioFocus(getApplicationContext());
        Picasso.with(getApplicationContext()).load(p.getImagePath()).into(this);
        mLockscreen.setLockscreenPlaying();
        mPlayer.start();
    }

    private void start(Intent intent) {

        mLockscreen = new LockscreenManager(getApplicationContext());
        mEpisodeId = intent.getExtras().getLong(EPISODE_EXTRA);
        if (mPlayer.getPlayingEpisodeId() != mEpisodeId) {
            EpisodeDao mEdao = new EpisodeDao(getApplicationContext());
            Episode episode = mEdao.get(mEpisodeId);
            Log.e(TAG, episode.getMp3());
            File file = new File(episode.getMp3());
            Log.e(TAG, file.getAbsolutePath());
            if (file.exists()) {
                mPlayer.setDataSource(episode);
                mPlayer.startAt(episode.getElapsed());
                mHandler.post(updateProgressRunnable);
                sentEpisodeMax(episode);
            }
        }
    }

    private void set(Intent intent) {
        mEpisodeId = intent.getExtras().getLong(EPISODE_EXTRA);
        if (mPlayer.getPlayingEpisodeId() != mEpisodeId) {
            EpisodeDao mEdao = new EpisodeDao(getApplicationContext());
            Episode episode = mEdao.get(mEpisodeId);
            Log.e(TAG, episode.getMp3());
            File file = new File(episode.getMp3());
            Log.e(TAG, file.getAbsolutePath());
            if (file.exists()) {
                mPlayer.setDataSource(episode);
                mPlayer.seek(episode.getElapsed());
                sentEpisodeMax(episode);
            }
        }
    }

    private void setForeground(boolean on) {
        if (on) {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            EpisodeDao mEdao = new EpisodeDao(getApplicationContext());
            Episode episode = mEdao.get(mEpisodeId);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    this)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(episode.getTitle())
                    .setContentText(episode.getDescription())
                    .setContentIntent(
                            PendingIntent.getActivity(this, 0,
                                    notificationIntent, 0));

            if (android.os.Build.VERSION.SDK_INT >= 16) {
                // mBuilder.addAction(R.drawable.ic_launcher, "Play",
                // PendingIntent.getActivity(this, 0, notificationIntent, 0));
            }
            Notification noti = mBuilder.build();
            startForeground(42, noti);
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
            Intent intent = new Intent(TIME_INTENT);
            intent.putExtra(TIME_EXTRA, mPlayer.getCurrentPosition());
            mBroadcaster.sendBroadcast(intent);
            mHandler.postDelayed(this, 1000);
        }

    };

    private void sentEpisodeMax(Episode episode) {
        Log.e("HAPT", "sentEpisodeMax");
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(episode.getMp3());
        String duration = mmr
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        Log.e("HAPT", "sentEpisodeMax" + duration);
        Intent intent = new Intent(MAX_INTENT);
        intent.putExtra(MAX_EXTRA, Integer.parseInt(duration));
        mBroadcaster.sendBroadcast(intent);
        mmr.release();

    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches(
                    AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                pause();
                Intent pauseIntent = new Intent(context, PlaybackService.class);
                pauseIntent.setAction(FOREGROUND_OFF_ACTION);
                startService(pauseIntent);
            }
        }

    };

    @Override
    public void onBitmapFailed(Drawable arg0) {
    }

    @Override
    public void onBitmapLoaded(Bitmap arg0, LoadedFrom arg1) {
        EpisodeDao edao = new EpisodeDao(getApplicationContext());
        PodcastDao pdao = new PodcastDao(getApplicationContext());
        Episode e = edao.get(mEpisodeId);
        Podcast p = pdao.get(e.getPodcast_id());
        mLockscreen.setMetadata(e, p, arg0);
    }

    @Override
    public void onPrepareLoad(Drawable arg0) {
    }

}
