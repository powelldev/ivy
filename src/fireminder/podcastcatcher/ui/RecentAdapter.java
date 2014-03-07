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
import android.view.View.OnClickListener;
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
import fireminder.podcastcatcher.utils.Helper;
import fireminder.podcastcatcher.utils.Utils;
import fireminder.podcastcatcher.valueobjects.Episode;
import fireminder.podcastcatcher.valueobjects.Podcast;

public class RecentAdapter extends CursorAdapter implements OnClickListener {

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

        Episode episode = EpisodeDao.cursorToEpisode(cursor);
        EpisodeReadableInfo info = new EpisodeReadableInfo(episode);

        TextView episodeTitle = (TextView) arg0
                .findViewById(R.id.list_item_recent_tv);
        TextView episodeDate = (TextView) arg0
                .findViewById(R.id.list_item_recent_date_tv);

        episodeDate.setText(info.getDate());
        episodeTitle.setText(info.getTitle());

        ImageView downloadOrPlayButton = (ImageView) arg0
                .findViewById(R.id.list_item_recent_download_or_play);
        ImageView queueButton = (ImageView) arg0
                .findViewById(R.id.list_item_recent_queue);
        
        if (Helper.isDownloaded(episode, context)){
            downloadOrPlayButton.setImageResource(R.drawable.ic_action_play_icon);
            queueButton.setVisibility(View.VISIBLE);
        } else {
            downloadOrPlayButton.setImageResource(R.drawable.ic_action_download);
            queueButton.setVisibility(View.GONE);
        }

        Listener listener = new Listener(episode.get_id());
        downloadOrPlayButton.setOnClickListener(listener);
        queueButton.setOnClickListener(listener);

        ImageView iv = (ImageView) arg0
                .findViewById(R.id.list_item_recent_album_iv);
        try {
            PodcastDao pdao = new PodcastDao(context);
            Podcast parent = pdao.get(episode.getPodcast_id());
            String path = parent.getImagePath();
            Picasso.with(arg1).load(path).noFade().fit().centerCrop()
                    .placeholder(R.drawable.ic_launcher).into(iv);

        } catch (NullPointerException e) {
            iv.setImageResource(R.drawable.ic_launcher);
            e.printStackTrace();
        }
    }

    private class Listener implements View.OnClickListener {

        private long mId;

        public Listener(long itemId) {
            mId = itemId;
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.list_item_recent_download_or_play) {
                EpisodeDao eDao = new EpisodeDao(context);
                Episode e = eDao.get(mId);
                Helper.downloadEpisodeMp3(e, context);

            } else if (v.getId() == R.id.list_item_recent_queue) {
                EpisodeDao eDao = new EpisodeDao(context);
                Episode e = eDao.get(mId);
                e.setPlaylistRank(eDao.getNumberOfEpisodesInPlaylist() + 1);
                eDao.update(e);
            }
        }

    }

    @Override
    public View newView(Context context, Cursor arg1, ViewGroup arg2) {
        View view = mLayoutInflater.inflate(R.layout.list_item_recent, arg2,
                false);
        return view;
    }

    @Override
    public void onClick(View view) {
    }
}
