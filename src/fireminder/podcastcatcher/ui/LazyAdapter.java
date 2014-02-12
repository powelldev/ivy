package fireminder.podcastcatcher.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.db.EpisodeDao2;
/*
 * Adapter for episodes
 *     manipulates custom listviews
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
    public Cursor getItem(int arg0) {
        // TODO Auto-generated method stub
        data.move(arg0);
        return data;
    }

    @Override
    public long getItemId(int arg0) {
        data.move(arg0-1);
        return data.getLong(data.getColumnIndex(EpisodeDao2.COLUMN_ID));
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

        title.setText(data.getString(data.getColumnIndex(EpisodeDao2.COLUMN_TITLE)));
        pubdate.setText(getDate(data.getLong(data.getColumnIndex(EpisodeDao2.COLUMN_PUBDATE)), "dd MMM"));
        byte[] size = data.getBlob(data.getColumnIndex(EpisodeDao2.COLUMN_MP3));
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
