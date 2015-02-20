package com.fireminder.podcastcatcher.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.fireminder.podcastcatcher.R;

public class NavigationDrawerAdapter extends ArrayAdapter<String> {

    String[] categories;

    public NavigationDrawerAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
        this.categories = objects;
    }

    class ViewHolder {
        TextView textView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.drawer_item, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.textView = (TextView) rowView.findViewById(R.id.drawer_item_textview);
            rowView.setTag(holder);
        }
        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.textView.setText(categories[position]);
        return rowView;
    }
}
