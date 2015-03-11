package com.fireminder.podcastcatcher.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.PopupMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.services.SearchAsyncTask;
import com.fireminder.podcastcatcher.services.SubscriptionService;
import com.fireminder.podcastcatcher.utils.PlaybackUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends ListFragment implements View.OnClickListener, SearchAsyncTask.SearchListener, View.OnKeyListener, AdapterView.OnItemClickListener {

  private EditText mSearchEditText;
  private List<SearchAsyncTask.SearchResult> mPodcasts;

  public SearchFragment() {
  }

  public static SearchFragment newInstance() {
    return new SearchFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_search, container, false);
    root.findViewById(R.id.fragment_search_search_button).setOnClickListener(this);
    mSearchEditText = (EditText) root.findViewById(R.id.fragment_search_search_edittext);
    mSearchEditText.setOnKeyListener(this);
    return root;
  }

  private void search() {
    final String inputText = mSearchEditText.getEditableText().toString();
    SearchAsyncTask task = new SearchAsyncTask(inputText, this);
    task.execute();
  }

  // Enter key launches search
  @Override
  public boolean onKey(View v, int keyCode, KeyEvent event) {
    if ((event.getAction() == KeyEvent.ACTION_DOWN) && keyCode == KeyEvent.KEYCODE_ENTER) {
      search();
      return true;
    }
    return false;
  }

  @Override
  public void onClick(View v) {
    search();
  }

  @Override
  public void onSearchComplete(List<SearchAsyncTask.SearchResult> podcasts) {
    if (podcasts == null) {
      Toast.makeText(getActivity(),
          "There was a problem loading search results. Please try again later",
          Toast.LENGTH_SHORT).show();
      return;
    }
    SearchAdapter adapter = new SearchAdapter(getActivity(), podcasts);
    mPodcasts = podcasts;
    getListView().setAdapter(adapter);
    getListView().setOnItemClickListener(this);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

  }

  private class SearchAdapter extends ArrayAdapter<SearchAsyncTask.SearchResult> implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    List<SearchAsyncTask.SearchResult> results;

    public SearchAdapter(Context context, List<SearchAsyncTask.SearchResult> results) {
      super(context, R.layout.list_item_people, results);
      this.results = results;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View row = convertView;
      ViewHolder holder = null;

      if (row == null) {
        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        row = inflater.inflate(R.layout.list_item_people, parent, false);

        holder = new ViewHolder();
        holder.title = (TextView) row.findViewById(R.id.title);
        holder.description = (TextView) row.findViewById(R.id.description);
        holder.image = (ImageView) row.findViewById(R.id.image);
        holder.actions = (ImageButton) row.findViewById(R.id.actions);

        row.setTag(holder);

      } else {
        holder = (ViewHolder) row.getTag();
      }
      row.setOnClickListener(this);
      row.setTag(R.id.TAG_PODCAST_KEY, position);

      holder.title.setText(results.get(position).title);
      holder.description.setText(results.get(position).description);
      holder.actions.setTag(R.id.TAG_PODCAST_KEY, position);
      holder.actions.setOnClickListener(this);
      holder.actions.setVisibility(View.GONE);
      Picasso.with(getActivity()).load(results.get(position).imageUri).into(holder.image);

      return row;
    }

    @Override
    public void onClick(View v) {
      int position = (int) v.getTag(R.id.TAG_PODCAST_KEY);
      switch (v.getId()) {
        case R.id.container:
          SubscriptionService.launchSubscriptionService(getActivity(), mPodcasts.get(position).feedUrl);
          Toast.makeText(getActivity(), "Adding " + mPodcasts.get(position).title, Toast.LENGTH_SHORT).show();
          break;
        case R.id.actions:
          PopupMenu popup = new PopupMenu(getContext(), v);
          popup.getMenuInflater().inflate(R.menu.menu_podcasts, popup.getMenu());
          popup.setOnMenuItemClickListener(this);
          popup.show();
          break;
      }
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
      return false;
    }
  }

  private static class ViewHolder {
    ViewGroup container;
    TextView title;
    TextView description;
    ImageView image;
    ImageButton actions;
  }
}
