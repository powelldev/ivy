package fireminder.podcastcatcher.receivers;

import fireminder.podcastcatcher.services.BootService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent("fireminder.podcastcatcher.boot.BootService");
        i.setClass(context, BootService.class);
        Log.e("HAPT", "Boot started");
        context.startService(i);
    }

}
