package com.fireminder.podcastcatcher.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
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

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends ListFragment implements View.OnClickListener, SearchAsyncTask.SearchListener {

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
        return root;
    }

    @Override
    public void onClick(View v) {
        final String inputText = mSearchEditText.getEditableText().toString();
        SearchAsyncTask task = new SearchAsyncTask(this.getActivity().getApplicationContext(),
                inputText, this);
        task.execute();
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
        Toast.makeText(getActivity(), "Adding " + mPodcasts.get(position).title, Toast.LENGTH_SHORT).show();
    }
}
