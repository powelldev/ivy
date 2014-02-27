package fireminder.podcastcatcher;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import fireminder.podcastcatcher.db.EpisodeDao;
import fireminder.podcastcatcher.db.PodcastDao;
import fireminder.podcastcatcher.receivers.MediaButtonReceiver;
import fireminder.podcastcatcher.valueobjects.Episode;
import fireminder.podcastcatcher.valueobjects.Podcast;

public class LockscreenManager {
    RemoteControlClient remoteControlClient;
    public LockscreenManager(Context context) {
        AudioManager audioManager = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);

        ComponentName myEventReceiver = new ComponentName(context,
                MediaButtonReceiver.class);
        audioManager.registerMediaButtonEventReceiver(myEventReceiver);
        if (remoteControlClient == null) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            intent.setComponent(myEventReceiver);
            remoteControlClient = new RemoteControlClient(
                    PendingIntent.getBroadcast(context, 0, intent, 0));
            audioManager.registerRemoteControlClient(remoteControlClient);
        }
        remoteControlClient
                .setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
        remoteControlClient
                .setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
                        | RemoteControlClient.FLAG_KEY_MEDIA_NEXT
                        | RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS);
    }

    public void requestAudioFocus(Context context) {
        AudioManager audioManager = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(new OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
            }
        },
        // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);
    }

    public void removeLockscreenControls(Context context) {
        AudioManager audioManager = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
        audioManager.unregisterRemoteControlClient(remoteControlClient);
    }

    public void setLockscreenPaused() {
        remoteControlClient
                .setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
    }

    public void setLockscreenPlaying() {
        remoteControlClient
                .setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
    }

    public void setMetadata(Episode e, Podcast p, Bitmap arg0) {
        remoteControlClient
                .editMetadata(true)
                .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST,
                        p.getTitle())
                .putString(MediaMetadataRetriever.METADATA_KEY_TITLE,
                        e.getTitle())
                .putBitmap(
                        RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK,
                        arg0
                        ).apply();
    }
}