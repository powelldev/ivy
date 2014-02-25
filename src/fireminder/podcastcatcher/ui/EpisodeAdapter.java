package fireminder.podcastcatcher.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
import android.widget.Toast;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.db.EpisodeDao;
import fireminder.podcastcatcher.utils.Utils;

public class EpisodeAdapter extends CursorAdapter {

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
    public void notifyDataSetChanged() {
        cursor.requery();
        super.notifyDataSetChanged();
    }



    @Override
    public void bindView(View arg0, Context arg1, Cursor cursor) {
        ImageView playIcon = (ImageView) arg0
                .findViewById(R.id.list_item_available_iv);
        TextView episodeTitle = (TextView) arg0
                .findViewById(R.id.list_item_episode_tv);
        TextView episodeDate = (TextView) arg0
                .findViewById(R.id.list_item_date_tv);


        long milliseconds = cursor.getLong(cursor.getColumnIndex(EpisodeDao.COLUMN_PUBDATE));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        Date date = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM");

        String duration = Utils.getStringFromCursor(cursor, EpisodeDao.COLUMN_DURATION);
        episodeDate.setText(sdf.format(date));
        episodeDate.setText(duration);
        episodeTitle.setText(Utils.getStringFromCursor(cursor, EpisodeDao.COLUMN_TITLE));
        
         
        ImageButton button = (ImageButton) arg0
                .findViewById(R.id.episode_popup_menu);
        button.setOnClickListener(new PopupListener(cursor.getLong(cursor
                .getColumnIndex(EpisodeDao.COLUMN_ID))));

        String file_mp3 = Utils.getStringFromCursor(cursor, EpisodeDao.COLUMN_MP3);
        if (file_mp3 != null) {
            File mp3 = new File(Utils.getStringFromCursor(cursor, EpisodeDao.COLUMN_MP3));
            if (mp3.exists()) {
                playIcon.setVisibility(View.VISIBLE);
                playIcon.setFocusable(false);
            } else {
                playIcon.setVisibility(View.GONE);
            }

        } else {
            playIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public View newView(Context context, Cursor arg1, ViewGroup arg2) {
        View view = mLayoutInflater.inflate(R.layout.episode_list_item, arg2,
                false);
        return view;
    }

    private class PopupListener implements View.OnClickListener,
            OnMenuItemClickListener {

        private long mId;

        public PopupListener(long itemId) {
            mId = itemId;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
            case R.id.menu_episode_delete:
                new EpisodeDao().clearDataOn(mId);
                notifyDataSetChanged();
                break;
            case R.id.menu_episode_delete_all:
                new EpisodeDao().clearDataOnAll(mId);
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
            inflater.inflate(R.menu.episode_menu, menu.getMenu());
            menu.show();
        }

    }

}
