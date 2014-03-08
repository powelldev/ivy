package fireminder.podcastcatcher.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import fireminder.podcastcatcher.R;

public class NavAdapter extends ArrayAdapter<String> {

    Context context;
    String[] categories;
    int textViewResourceId;

    int boldedItem = 0;
    public void setBold(int position) {
        boldedItem = position;
        this.notifyDataSetChanged();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(textViewResourceId, parent, false);
        } else {
        }

        TextView tv = (TextView) row.findViewById(R.id.list_item_nav_tv);
        tv.setText(categories[position]);
        if (position == boldedItem) {
            tv.setTypeface(null, Typeface.BOLD);
        } else {
            tv.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        }

        return row;
    }

    public NavAdapter(Context context, int textViewResourceId, String[] objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        this.textViewResourceId = textViewResourceId;
        this.categories = objects;
    }
}
