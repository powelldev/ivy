package fireminder.podcastcatcher.ui;

import java.io.ByteArrayInputStream;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.db.PodcastDao2;

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
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();

			ByteArrayInputStream is = new ByteArrayInputStream(cursor.getBlob(cursor.getColumnIndex(PodcastDao2.COLUMN_IMAGELINK)));
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			Bitmap image = BitmapFactory.decodeStream(is, null, options);

			ByteArrayInputStream is2 = new ByteArrayInputStream(cursor.getBlob(cursor.getColumnIndex(PodcastDao2.COLUMN_IMAGELINK)));
			BitmapFactory.Options options2 = new BitmapFactory.Options();
			options2.inSampleSize = calculateInSampleSize(options, display.getWidth(), 96);
			Log.e("TAJEFIOJFEOJFAOI", display.getWidth() + " " + options2.inSampleSize);
			Bitmap image2 = BitmapFactory.decodeStream(is2, null, options2);

			iv.setImageBitmap(image2);

		} catch (NullPointerException e){
			iv.setImageResource(R.drawable.ic_launcher);
			e.printStackTrace();
		}
		
		
		TextView tv = (TextView) arg0.findViewById(R.id.podcast_tv);
		tv.setText(cursor.getString(cursor.getColumnIndex(PodcastDao2.COLUMN_TITLE)));
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		View view = mInflater.inflate(R.layout.podcast_adapter, arg2, false);
		return view;
	}
	public static int calculateInSampleSize(
	        BitmapFactory.Options options, int reqWidth, int reqHeight) {
	// Raw height and width of image
	final int height = options.outHeight;
	final int width = options.outWidth;
	int inSampleSize = 1;
	 
	if (height > reqHeight || width > reqWidth) {
	 
	    // Calculate ratios of height and width to requested height and width
	    final int heightRatio = Math.round((float) height / (float) reqHeight);
	    final int widthRatio = Math.round((float) width / (float) reqWidth);
	 
	    // Choose the smallest ratio as inSampleSize value, this will guarantee
	    // a final image with both dimensions larger than or equal to the
	    // requested height and width.
	    inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	}
	// According to Android Dev Bytes, Bitmap.decodeStream is 
	// 1) faster when dealing with powers of 2
	// 2) has no difference in scaling for non-powers of 2
	// This returns the closest power of 2: 2 ^ (log2 inSampleSize) 
	// @author: powelldev
	return (int) Math.pow(2, Math.ceil(Math.log10(inSampleSize)/Math.log10(2)));
	 
	}

}
