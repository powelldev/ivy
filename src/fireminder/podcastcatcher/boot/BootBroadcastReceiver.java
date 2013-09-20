package fireminder.podcastcatcher.boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent i = new Intent("fireminder.podcastcatcher.boot.BootService");
		i.setClass(context, BootService.class);
		context.startService(i);
	}

}
