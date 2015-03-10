package com.fireminder.podcastcatcher.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.fireminder.podcastcatcher.R;
import com.fireminder.podcastcatcher.services.SearchAsyncTask;
import com.fireminder.podcastcatcher.services.SubscriptionService;
import com.fireminder.podcastcatcher.utils.PlaybackUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends ListFragment implements View.OnClickListener, SearchAsyncTask.SearchListener, View.OnKeyListener {

  private EditText mSearchEditText;
  private ArrayAdapter<String> mAdapter;
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
    List<String> stringsForAdapter = new ArrayList<>(podcasts.size());
    for (SearchAsyncTask.SearchResult result : podcasts) {
      stringsForAdapter.add(result.title);
    }
    mAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, stringsForAdapter);
    mPodcasts = podcasts;
    getListView().setAdapter(mAdapter);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);
    SubscriptionService.launchSubscriptionService(getActivity(), mPodcasts.get(position).feedUrl);
    // TODO download first episode
    Toast.makeText(getActivity(), "Adding " + mPodcasts.get(position).title, Toast.LENGTH_SHORT).show();
  }


}
