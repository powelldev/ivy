package fireminder.podcastcatcher.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
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
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.db.EpisodeDao;
import fireminder.podcastcatcher.utils.Utils;
import fireminder.podcastcatcher.valueobjects.Episode;

public class PlaylistAdapter extends CursorAdapter {

    private Context context;
    private Cursor cursor;
    private final LayoutInflater mLayoutInflater;

    public PlaylistAdapter(Context context, Cursor c, int flags) {

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
        Episode episode = EpisodeDao.cursorToEpisode(cursor);
        EpisodeReadableInfo info = new EpisodeReadableInfo(episode);

        ImageView playIcon = (ImageView) arg0
                .findViewById(R.id.playlist_item_album_iv);
        TextView episodeTitle = (TextView) arg0
                .findViewById(R.id.playlist_item_episode_tv);
        TextView episodeDate = (TextView) arg0
                .findViewById(R.id.playlist_item_date_tv);

        episodeDate.setText(info.getDate());
        episodeTitle.setText(info.getTitle());

        ImageButton button = (ImageButton) arg0
                .findViewById(R.id.playlist_popup_menu);
        PopupListener listener = new PopupListener(info.getId());
        button.setOnClickListener(listener);

        if (info.isDownloaded()) {
            playIcon.setVisibility(View.VISIBLE);
            playIcon.setFocusable(false);
        } else {
            playIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public View newView(Context context, Cursor arg1, ViewGroup arg2) {
        View view = mLayoutInflater.inflate(R.layout.list_item_playlist, arg2,
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
                new EpisodeDao(context).clearDataOn(mId);
                notifyDataSetChanged();
                break;
            case R.id.menu_episode_delete_all:
                new EpisodeDao(context).clearDataOnAll(mId);
                notifyDataSetChanged();
                break;
            case R.id.menu_episode_queue:
                EpisodeDao eDao = new EpisodeDao(context);
                Episode e = eDao.get(mId);
                e.setPlaylistRank(eDao.getNumberOfEpisodesInPlaylist() + 1);
                eDao.update(e);
                Log.e(Utils.TAG,
                        "Playlsit set: " + eDao.getNumberOfEpisodesInPlaylist());
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
