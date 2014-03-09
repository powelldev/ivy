package fireminder.podcastcatcher.services;

import java.io.File;
import java.util.List;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

import fireminder.podcastcatcher.LockscreenManager;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.StatefulMediaPlayer;
import fireminder.podcastcatcher.activities.MainActivity;
import fireminder.podcastcatcher.db.EpisodeDao;
import fireminder.podcastcatcher.db.PodcastDao;
import fireminder.podcastcatcher.fragments.SettingsFragment;
import fireminder.podcastcatcher.utils.Utils;
import fireminder.podcastcatcher.valueobjects.Episode;
import fireminder.podcastcatcher.valueobjects.Podcast;

public class PlaybackService extends Service implements Target,
        OnCompletionListener {

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

    public static final String EPISODE_CHANGE_INTENT = "fireminder.podcastcatcher.services.PlaybackService.EPISODE_CHANGE";
    public static final String EPISODE_ID_EXTRA = "episode_id";

    public static final long NO_EPISODE = -1;

    public static final String STOP_ACTION = "STOP";

    private int mElapsed;
    private StatefulMediaPlayer mPlayer;
    private LocalBroadcastManager mBroadcaster;
    private LockscreenManager mLockscreen;

    @Override
    public void onCreate() {
        mPlayer = new StatefulMediaPlayer();
        mPlayer.setOnCompletionListener(this);
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
        if (null == action) {
            return Service.START_NOT_STICKY;
        }
        if (action == START_ACTION) {
            prepare(intent);
            startPlaying();

        } else if (action == PLAY_PAUSE_ACTION) {
            resumeOrPause();

        } else if (action == FORWARD_ACTION) {
            seekQuantum(30000);

        } else if (action == REWIND_ACTION) {
            seekQuantum(-30000);

        } else if (action == SEEK_ACTION) {
            int time = intent.getIntExtra(SEEK_EXTRA, 0);
            seekToAndResume(time);

        } else if (action == FOREGROUND_ON_ACTION) {
            if (mPlayer.isPlaying())
                setForeground(true);

        } else if (action == FOREGROUND_OFF_ACTION) {
            setForeground(false);
        } else if (action == STOP_ACTION) {
            if (!mPlayer.isPlaying()) {
                this.stopSelf();
            }
        }
        return Service.START_NOT_STICKY;
    }

    private void prepare(Intent intent) {
        updateEpisodeElapsed();
        long id = pullIdFromIntent(intent);
        setCurrentEpisodeId(id);
    }

    private long pullIdFromIntent(Intent intent) {
        long id = intent.getExtras().getLong(EPISODE_EXTRA);
        return id;
    }

    private void setCurrentEpisodeId(long id) {
        setSharedPreferenceEpisodePlaying(getApplicationContext(), id);
    }

    private void startPlaying() {
        Episode episode = pullCurrentEpisode();
        Log.e(Utils.TAG, "Episode id: " + episode.get_id());
        Log.e(Utils.TAG,
                "Elapsed for starting episode: " + episode.getElapsed());
        File file = new File(episode.getMp3());
        if (file.exists()) {
            mPlayer.setDataSource(episode);
            mPlayer.startAt(episode.getElapsed());
            setupUiElements(episode);
            setupLockscreen(episode);
        }
    }

    private Episode pullCurrentEpisode() {
        EpisodeDao mEdao = new EpisodeDao(getApplicationContext());
        long id = getSharedPreferenceEpisodePlaying(getApplicationContext());
        if (id == NO_EPISODE)
            throw new RuntimeException("No episode set in PlaybackService");
        return mEdao.get(id);
    }

    private void setupLockscreen(Episode episode) {
        Podcast p = new PodcastDao(getApplicationContext()).get(episode
                .getPodcast_id());
        mLockscreen = new LockscreenManager(getApplicationContext());
        Picasso.with(getApplicationContext()).load(p.getImagePath()).into(this);
        mLockscreen.requestAudioFocus(getApplicationContext());
        mLockscreen.setLockscreenPlaying();
    }

    private void setupUiElements(Episode episode) {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.post(updateProgressRunnable);
        sentEpisodeMax(episode);
    }

    private void resumeOrPause() {
        if (mPlayer.isPlaying()) {
            pause();
        } else {
            startPlaying();
        }
    }

    private void seekQuantum(int delta) {
        int updatedTime = mPlayer.getCurrentPosition() + delta;
        if (updatedTime < 0)
            updatedTime = 0;
        seekToAndResume(updatedTime);
    }

    private void seekToAndResume(int target) {
        boolean player = mPlayer.isPlaying();
        mPlayer.pause();
        mPlayer.seek(target);
        if (player)
            mPlayer.start();

    }

    private void pause() {
        updateEpisodeElapsed();
        mPlayer.pause();
        mHandler.removeCallbacksAndMessages(null);
        mLockscreen.setLockscreenPaused();
    }

    private void updateEpisodeElapsed() {
        if (getSharedPreferenceEpisodePlaying(getApplicationContext()) != -1) {
            EpisodeDao mEdao = new EpisodeDao(getApplicationContext());
            Episode episode = mEdao
                    .get(getSharedPreferenceEpisodePlaying(getApplicationContext()));
            episode.setElapsed(mElapsed);
            long id = mEdao.update(episode);
            Log.e(Utils.TAG, id + " Elapsed after insert: " + mEdao.get(id));
        }
    }

    private void setForeground(boolean on) {
        Log.e(Utils.TAG, "setForeground" + on);
        if (on) {
            Episode episode = pullCurrentEpisode();
            Intent notificationIntent = new Intent(this, MainActivity.class);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    this)
                    .setSmallIcon(R.drawable.ic_white_icon)
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
            if (!mPlayer.isPlaying()) {
                this.stopSelf();
            }
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
            Log.e(Utils.TAG, "Time update Runnable running");
            try {
                mElapsed = mPlayer.getCurrentPosition();
                Intent intent = new Intent(TIME_INTENT);
                intent.putExtra(TIME_EXTRA, mPlayer.getCurrentPosition());
                mBroadcaster.sendBroadcast(intent);
                mHandler.postDelayed(this, 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    private void sentEpisodeMax(Episode episode) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(episode.getMp3());
        String duration = mmr
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
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
        PodcastDao pdao = new PodcastDao(getApplicationContext());
        Episode e = pullCurrentEpisode();
        Podcast p = pdao.get(e.getPodcast_id());
        mLockscreen.setMetadata(e, p, arg0);
    }

    @Override
    public void onPrepareLoad(Drawable arg0) {
    }

    @Override
    public void onCompletion(MediaPlayer arg0) {
        Log.e(Utils.TAG, "complete");
        mPlayer.setPlaybackCompleted();
        mPlayer.seek(0);

        EpisodeDao edao = new EpisodeDao(getApplicationContext());
        Episode e = pullCurrentEpisode();
        if (isSharedPreferenceAutoDelete(getApplicationContext())) {
            edao.clearDataOn(e.get_id());
            e.setElapsed(0);
            e.setMp3("");
        }
        e.setPlaylistRank(-1);
        edao.update(e);

        List<Episode> playlist = edao.getPlaylistEpisodes();
        if (playlist != null && playlist.size() != 0) {
            setCurrentEpisodeId(playlist.get(0).get_id());
            sendNewEpisodeBroadcastToMainActivity();
            startPlaying();
        } else {
            setCurrentEpisodeId(NO_EPISODE);
            this.stopSelf();
        }

    }

    private void sendNewEpisodeBroadcastToMainActivity() {
        Intent intent = new Intent(EPISODE_CHANGE_INTENT);
        intent.putExtra(EPISODE_ID_EXTRA,
                getSharedPreferenceEpisodePlaying(getApplicationContext()));
        mBroadcaster.sendBroadcast(intent);
    }

    public static boolean isSharedPreferenceAutoDelete(Context context) {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(context);
        return settings.getBoolean(
                context.getResources().getString(R.string.prefAutoDelete),
                false);
    }

    public static void setSharedPreferenceEpisodePlaying(Context context,
            long id) {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(MainActivity.EPISODE_PLAYING, id);
        editor.commit();
    }

    public static long getSharedPreferenceEpisodePlaying(Context context) {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(context);
        return settings.getLong(MainActivity.EPISODE_PLAYING, -1);
    }

}
