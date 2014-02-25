package fireminder.podcastcatcher.receivers;

import fireminder.podcastcatcher.services.PlaybackService;
import fireminder.podcastcatcher.utils.Utils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

public class MediaButtonReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            Log.e(Utils.TAG, "Media button pressed");
            KeyEvent event = (KeyEvent) intent
                    .getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (KeyEvent.ACTION_DOWN == event.getAction()) {
                if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == event.getKeyCode()) {
                    Intent sIntent = new Intent(context, PlaybackService.class);
                    sIntent.setAction("fireminder.podcastcatcher.REWIND");
                    context.startService(sIntent);
                } else if (KeyEvent.KEYCODE_MEDIA_NEXT == event
                        .getKeyCode()) {
                    Intent sIntent = new Intent(context, PlaybackService.class);
                    sIntent.setAction("fireminder.podcastcatcher.FAST_FORWARD");
                    context.startService(sIntent);
                } else if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == event
                        .getKeyCode()) {
                    Intent sIntent = new Intent(context, PlaybackService.class);
                    sIntent.setAction("fireminder.podcastcatcher.PLAY_PAUSE");
                    context.startService(sIntent);
                }
            }
        }
    }

}
