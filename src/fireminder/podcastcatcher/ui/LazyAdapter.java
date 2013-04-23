package fireminder.podcastcatcher.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.db.EpisodeSqlHelper;
/*
 * Adapter for episodes
 * 	manipulates custom listviews
 */
public class LazyAdapter extends BaseAdapter{
	private Context activity;
	private Cursor data;
	private static LayoutInflater inflater = null;
	
	public LazyAdapter(Context context, Cursor c){
		activity = context;
		data = c;
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.getCount();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		if(convertView == null)
			vi = inflater.inflate(R.layout.lazy_row, null);

		TextView title = (TextView) vi.findViewById(R.id.title);
		TextView pubdate = (TextView) vi.findViewById(R.id.pubdate);
		TextView downloaded = (TextView) vi.findViewById(R.id.download);
		data.moveToPosition(position);

		title.setText(data.getString(data.getColumnIndex(EpisodeSqlHelper.COLUMN_TITLE)));
		pubdate.setText(getDate(data.getLong(data.getColumnIndex(EpisodeSqlHelper.COLUMN_PUBDATE)), "dd MMM"));
		byte[] size = data.getBlob(data.getColumnIndex(EpisodeSqlHelper.COLUMN_MP3));
		if(size != null){
			downloaded.setText("" + size.length);
		}
		return vi;
	}
	
	public static String getDate(long millis, String dateFormat){
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);
		return formatter.format(calendar.getTime());
	}

}
