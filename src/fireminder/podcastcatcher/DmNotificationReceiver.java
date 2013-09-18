package fireminder.podcastcatcher;

import java.io.File;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

public class DmNotificationReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		if (arg1.getAction()
				.equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
			DownloadManager dm = (DownloadManager) arg0
					.getSystemService(Context.DOWNLOAD_SERVICE);
			Query query = new Query();
			query.setFilterByStatus(DownloadManager.STATUS_PENDING
					| DownloadManager.STATUS_RUNNING);
			Cursor c = dm.query(query);
			c.moveToFirst();
			do {
				dm.remove(c.getInt(c.getColumnIndex(DownloadManager.COLUMN_ID)));
				String uri = c.getString(c
						.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
				try {
					File file = new File(uri);
					boolean deleted = file.delete();
					if (deleted)
						Log.e("Downloading file", "was deleted");
				} catch (NullPointerException e) {
					// only caught if file hasn't been downloaded yet. Ignore.
				}
			} while (c.moveToNext());

		}
	}

}
