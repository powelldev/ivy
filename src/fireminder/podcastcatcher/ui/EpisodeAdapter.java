package fireminder.podcastcatcher.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.db.EpisodeSqlHelper;

public class EpisodeAdapter extends CursorAdapter{

	private Context context; 
	private Cursor cursor;
	private final LayoutInflater mLayoutInflater;
	
	public EpisodeAdapter(Context context, Cursor c, int flags) {
		
		super(context, c, flags);
		mLayoutInflater = LayoutInflater.from(context);
		this.context = context;
		this.cursor = c;
	}

	@Override
	public void bindView(View arg0, Context arg1, Cursor cursor) {
		TextView episodeTitle = (TextView) arg0.findViewById(R.id.list_item_episode_tv);
		episodeTitle.setText(cursor.getString(cursor.getColumnIndex(EpisodeSqlHelper.COLUMN_TITLE)));
		
		TextView episodeDate = (TextView) arg0.findViewById(R.id.list_item_date_tv);
		long milliseconds = cursor.getLong(cursor.getColumnIndex(EpisodeSqlHelper.COLUMN_PUBDATE));
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(milliseconds);
		Date date = calendar.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM");
		episodeDate.setText(sdf.format(date));
		
		ImageButton playIcon = (ImageButton) arg0.findViewById(R.id.play_icon_iv);
		
		if(cursor.getString(cursor.getColumnIndex(EpisodeSqlHelper.COLUMN_MP3)) != null){
			playIcon.setVisibility(View.VISIBLE);
			playIcon.setFocusable(false);
		}
	}

	@Override
	public View newView(Context context, Cursor arg1, ViewGroup arg2) {
		View view = mLayoutInflater.inflate(R.layout.episode_list_item, arg2, false);
		return view;
	}


}
