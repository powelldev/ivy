package fireminder.podcastcatcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DownloadBroadcastReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		arg0.startService(arg1);
	}

}
