package fireminder.podcastcatcher.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.db.EpisodeDao;
import fireminder.podcastcatcher.db.PodcastDao;
import fireminder.podcastcatcher.utils.Utils;
import fireminder.podcastcatcher.valueobjects.Podcast;

public class RecentAdapter extends CursorAdapter {

    private Context context;
    private Cursor cursor;
    private final LayoutInflater mLayoutInflater;

    public RecentAdapter(Context context, Cursor c, int flags) {

        super(context, c, flags);
        mLayoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.cursor = c;
    }

    @Override
    public void notifyDataSetChanged() {
        cursor.requery();
        super.notifyDataSetChanged();
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void bindView(View arg0, Context arg1, Cursor cursor) {
        TextView episodeTitle = (TextView) arg0
                .findViewById(R.id.list_item_recent_tv);
        TextView episodeDate = (TextView) arg0
                .findViewById(R.id.list_item_recent_date_tv);

        long milliseconds = cursor.getLong(cursor
                .getColumnIndex(EpisodeDao.COLUMN_PUBDATE));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        Date date = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM");

        String duration = Utils.getStringFromCursor(cursor,
                EpisodeDao.COLUMN_DURATION);
        episodeDate.setText(sdf.format(date));
        episodeDate.setText(duration);
        episodeTitle.setText(Utils.getStringFromCursor(cursor,
                EpisodeDao.COLUMN_TITLE));

        ImageButton button = (ImageButton) arg0
                .findViewById(R.id.list_item_recent_menu_button);
        button.setOnClickListener(new PopupListener(cursor.getLong(cursor
                .getColumnIndex(EpisodeDao.COLUMN_ID))));

        ImageView iv = (ImageView) arg0.findViewById(R.id.list_item_recent_album_iv);
        try {
            long id = cursor.getLong(cursor
                    .getColumnIndex(EpisodeDao.COLUMN_PODCAST_ID));
            PodcastDao pdao = new PodcastDao(context);
            Podcast parent = pdao.get(id);
            String path = parent.getImagePath();
            Picasso.with(arg1).load(path).noFade().fit().centerCrop()
                    .placeholder(R.drawable.ic_launcher).into(iv);

        } catch (NullPointerException e) {
            iv.setImageResource(R.drawable.ic_launcher);
            e.printStackTrace();
        }
    }

    private class PopupListener implements View.OnClickListener,
            OnMenuItemClickListener {

        private long mId;

        public PopupListener(long itemId) {
            mId = itemId;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menu) {
            switch (menu.getItemId()) {
            case R.id.menu_podcast_delete:
                new EpisodeDao(context).clearDataOn(mId);
                notifyDataSetChanged();
                break;
            }
            return false;
        }

        @Override
        public void onClick(View v) {
            PopupMenu menu = new PopupMenu(context, v);
            menu.setOnMenuItemClickListener(this);
            MenuInflater inflater = menu.getMenuInflater();
            inflater.inflate(R.menu.menu_podcast, menu.getMenu());
            menu.show();
        }

    }

    @Override
    public View newView(Context context, Cursor arg1, ViewGroup arg2) {
        View view = mLayoutInflater.inflate(R.layout.list_item_recent, arg2,
                false);
        return view;
    }
}
