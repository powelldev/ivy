package fireminder.podcastcatcher.receivers;

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
            if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
                Log.e(Utils.TAG, "Play key");
            }
        }
    }

}
