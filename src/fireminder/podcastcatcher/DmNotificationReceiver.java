package fireminder.podcastcatcher;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class DmNotificationReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		if(arg1.getAction().equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)){
			long [] id = arg1.getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
			DownloadManager dm = (DownloadManager) arg0.getSystemService(Context.DOWNLOAD_SERVICE);
			dm.remove(id);
			Toast.makeText(arg0, "Canceled", Toast.LENGTH_LONG).show();
		}
	}

}
