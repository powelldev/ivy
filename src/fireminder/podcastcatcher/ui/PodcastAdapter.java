package fireminder.podcastcatcher.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.db.EpisodeDao;
import fireminder.podcastcatcher.db.PodcastDao;
import fireminder.podcastcatcher.utils.Utils;
import fireminder.podcastcatcher.valueobjects.Episode;

public class PodcastAdapter extends CursorAdapter {

    public final LayoutInflater mInflater;
    public Context context;
    public Cursor cursor;

    public PodcastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.cursor = c;
    }

    @Override
    public void notifyDataSetChanged() {
        cursor.requery();
        super.notifyDataSetChanged();
    }

    @Override
    public void bindView(View arg0, Context arg1, Cursor arg2) {
        ImageView iv = (ImageView) arg0.findViewById(R.id.podcast_iv);

        try {
            WindowManager wm = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();

            Picasso.with(arg1)
            .load(Utils.getStringFromCursor(arg2, PodcastDao.COLUMN_IMAGELINK))
            .noFade()
            .fit()
            .centerCrop()
            .placeholder(R.drawable.ic_launcher)
            .into(iv);

        } catch (NullPointerException e) {
            iv.setImageResource(R.drawable.ic_launcher);
            e.printStackTrace();
        }

        try {
            TextView descripTv = (TextView) arg0.findViewById(R.id.descrip);
            long id = cursor.getLong(cursor
                    .getColumnIndex(PodcastDao.COLUMN_ID));
            Episode e = new EpisodeDao().getLatestEpisode(id);
            String descrip = e.getDescription();
            descrip = android.text.Html.fromHtml(descrip).toString();
            int descripSize = 125;
            if (descrip.length() > descripSize) {
                descrip = descrip.substring(0, descripSize - 3) + "...";
            }
            descripTv.setText(descrip);

            TextView dateTv = (TextView) arg0.findViewById(R.id.date_tv);
            long milliseconds = e.getPubDate();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(milliseconds);
            Date date = calendar.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM");
            dateTv.setText(sdf.format(date));

            ImageView starIv = (ImageView) arg0.findViewById(R.id.star_iv);
            if (e.getElapsed()<= 0 && new File(e.getMp3()).exists()) {
                starIv.setVisibility(View.VISIBLE);
            } else if (e.getElapsed() > 30000) {
                starIv.setVisibility(View.INVISIBLE);
            } else {
                starIv.setVisibility(View.INVISIBLE);
            }
        } catch (Exception xe) {
            Log.e("Err", "Unable to load episode: " + xe.getMessage());
        }
        TextView tv = (TextView) arg0.findViewById(R.id.podcast_tv);
        String title = Utils.getStringFromCursor(cursor,
                PodcastDao.COLUMN_TITLE);
        int textViewSize = 32;
        if (title.length() > textViewSize) {
            title = title.substring(0, textViewSize - 3) + "...";
        }
        tv.setText(title);
    }

    @Override
    public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
        View view = mInflater.inflate(R.layout.fragment_podcast, arg2, false);
        return view;
    }

    /*
    public static Bitmap getBitmapFromPodcast(Podcast podcast) {
        WindowManager wm = (WindowManager) PodcastCatcher.getInstance()
                .getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(podcast.getImagePath(), options);

        BitmapFactory.Options options2 = new BitmapFactory.Options();
        options2.inSampleSize = calculateInSampleSize(options, R.dimen.header_height, R.dimen.header_height);
        Bitmap image2 = BitmapFactory.decodeFile(podcast.getImagePath(), options2);
        return image2;
    }
    */
    /*

    public static int calculateInSampleSize(BitmapFactory.Options options,
            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return (int) Math.pow(2,
                Math.ceil(Math.log10(inSampleSize) / Math.log10(2)));

    }
    */

}
