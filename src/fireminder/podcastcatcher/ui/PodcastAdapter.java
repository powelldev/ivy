package fireminder.podcastcatcher.ui;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import org.ocpsoft.prettytime.PrettyTime;

import android.app.ProgressDialog;
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

import com.squareup.picasso.Picasso;

import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.db.EpisodeDao;
import fireminder.podcastcatcher.db.PodcastDao;
import fireminder.podcastcatcher.utils.Utils;
import fireminder.podcastcatcher.valueobjects.Episode;
import fireminder.podcastcatcher.valueobjects.Podcast;

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

    private void setupEpisodeInfo(Podcast podcast, View view, Context context) {
        EpisodeDao edao = new EpisodeDao(context);
        Episode episode = edao.getLatestEpisode(podcast.getId());
        EpisodeReadableInfo info = new EpisodeReadableInfo(episode);

        ((TextView) view.findViewById(R.id.descrip)).setText(info.getTitle());
        ((TextView) view.findViewById(R.id.date_tv)).setText(info.getDate());

        ImageButton button = (ImageButton) view
                .findViewById(R.id.podcast_popup_menu);
        button.setOnClickListener(new PopupListener(info.getId()));

        ImageView starIv = (ImageView) view.findViewById(R.id.star_iv);
        if (info.isDownloaded()) {
            starIv.setVisibility(View.VISIBLE);
        } else {
            starIv.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void bindView(View view, Context arg1, Cursor arg2) {

        Podcast podcast = PodcastDao.createPodcastFromCursor(cursor);

        ImageView iv = (ImageView) view.findViewById(R.id.podcast_iv);

        try {
            String uri = podcast.getImagePath();
            Picasso.with(arg1).load(uri).noFade().fit().centerCrop()
                    .placeholder(R.drawable.ic_launcher).into(iv);
        } catch (NullPointerException e) {
            iv.setImageResource(R.drawable.ic_launcher);
            e.printStackTrace();
        }

        try {
        setupEpisodeInfo(podcast, view, context);
        } catch (Exception e) {
            
        }

        TextView tv = (TextView) view.findViewById(R.id.podcast_tv);
        String title;
        try {
            title = podcast.getTitle();
            int textViewSize = 32;
            if (title.length() > textViewSize) {
                title = title.substring(0, textViewSize - 3) + "...";
            }
            tv.setText(title);
        } catch (Exception e) {
            tv.setText("Refresh");
        }
    }

    @Override
    public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
        View view = mInflater.inflate(R.layout.fragment_podcast, arg2, false);
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
            case R.id.menu_podcast_delete:
                ProgressDialog dialog = new ProgressDialog(context);
                dialog.setTitle("Removing");
                dialog.show();
                new PodcastDao(context).delete(mId);
                notifyDataSetChanged();
                dialog.dismiss();
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
}
