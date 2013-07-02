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
import android.widget.TextView;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.db.Episode;
import fireminder.podcastcatcher.db.EpisodeDAO;
import fireminder.podcastcatcher.db.PlaylistSqlHelper;

public class PlaylistAdapter extends CursorAdapter{
	Context context;
	Cursor cursor;
	LayoutInflater mInflater;
	EpisodeDAO edao;
	public PlaylistAdapter(Context context, Cursor cursor, int flags) {
		super(context, cursor, flags);
		this.context = context;
		this.cursor = cursor;
		edao = new EpisodeDAO(context);
		edao.open();
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		long eid = cursor.getLong(cursor.getColumnIndex(PlaylistSqlHelper.COLUMN_EPISODE_ID));
		Episode episode = edao.getEpisode(eid);
		
		
		TextView episodeTitle = (TextView) view.findViewById(R.id.playlist_item_episode_tv);
		episodeTitle.setText(episode.getTitle());
		
		TextView episodeDate = (TextView) view.findViewById(R.id.playlist_item_date_tv);
		long milliseconds = episode.getPubDate();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(milliseconds);
		Date date = calendar.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM");
		episodeDate.setText(sdf.format(date));
		
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		View view = mInflater.inflate(R.layout.playlist_list_item, arg2, false);
		return view;
	}

}
