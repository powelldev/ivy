package fireminder.podcastcatcher.ui;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.valueobjects.Episode;

public class PlaylistAdapter extends ArrayAdapter<Episode> {

    Context context;

    public PlaylistAdapter(Context context, int textViewResourceId,
            List<Episode> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.episode_list_item, null);
        }

        Episode item = getItem(position);
        if (item != null) {
            // My layout has only one TextView
            TextView itemView = (TextView) view
                    .findViewById(R.id.list_item_episode_tv);
            if (itemView != null) {
                itemView.setText(item.getTitle());
            }
        }

        return view;
    }

}
