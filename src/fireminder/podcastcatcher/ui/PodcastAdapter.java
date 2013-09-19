package fireminder.podcastcatcher.ui;

import java.io.ByteArrayInputStream;

import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.db.PodcastSqlHelper;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class PodcastAdapter extends CursorAdapter{
	
	public final LayoutInflater mInflater;
	public Context context;
	public Cursor cursor;
	public PodcastAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		this.context= context;
		this.mInflater = LayoutInflater.from(context);
		this.cursor = c;
	}


	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {
		ImageView iv = (ImageView) arg0.findViewById(R.id.podcast_iv);
		
		try {
			ByteArrayInputStream is = new ByteArrayInputStream(cursor.getBlob(cursor.getColumnIndex(PodcastSqlHelper.COLUMN_IMAGELINK)));
			
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 4;
			Bitmap image = BitmapFactory.decodeStream(is, null, options);
			iv.setImageBitmap(image);
		} catch (Exception e){
			iv.setImageResource(R.drawable.ic_launcher);
			e.printStackTrace();
		}
		
		TextView tv = (TextView) arg0.findViewById(R.id.podcast_tv);
		tv.setText(cursor.getString(cursor.getColumnIndex(PodcastSqlHelper.COLUMN_TITLE)));
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		View view = mInflater.inflate(R.layout.podcast_adapter, arg2, false);
		return view;
	}

}
