package fireminder.podcastcatcher.fragments;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.activities.MainActivity;
import fireminder.podcastcatcher.activities.SearchActivity;
import fireminder.podcastcatcher.db.EpisodeDao;
import fireminder.podcastcatcher.db.PodcastDao;
import fireminder.podcastcatcher.downloads.BackgroundThread;
import fireminder.podcastcatcher.ui.PodcastAdapter;
import fireminder.podcastcatcher.utils.Utils;

@SuppressWarnings("deprecation")/* ClipboardManager unlikely to be removed */
public class PodcastFragment extends ListFragment {

    final static String TAG = PodcastFragment.class.getSimpleName();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListView listView = getListView();
        listView.setOnItemClickListener(channelListViewOnClickListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_podcast, container,
                false);
        setupListAdapter();
        subscribeIfIntent();
        return rootView;
    }
    
    private void setupListAdapter()  {
        PodcastDao pdao = new PodcastDao(getActivity());
        Cursor podcastCursor = pdao.getAllPodcastsAsCursor();
        PodcastAdapter cursorAdapter = new PodcastAdapter(getActivity(),
                podcastCursor, 0);
        setListAdapter(cursorAdapter);
    }

    private void subscribeIfIntent() {
        try {
        Bundle b =  getArguments();
        String url = b.getString("uri");
        subscribe(url);
        } catch (NullPointerException e) {
            // There are no extras in bundle. We can safely ignore.
        }
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        registerForContextMenu(getListView());
        getActivity().getActionBar().setBackgroundDrawable(Utils.action_bar_blue);
        this.getListView().getEmptyView()
                .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(),
                                SearchActivity.class);
                        getActivity().startActivityForResult(i, 42);
                    }

                });
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_podcast, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();
        switch (item.getItemId()) {
        case R.id.menu_podcast_delete:
            deletePodcast(info.id);
            break;
        }
        return super.onContextItemSelected(item);
    }

    private void deletePodcast(long id) {
        PodcastDao pdao = new PodcastDao(getActivity());
        EpisodeDao edao = new EpisodeDao(getActivity());
        pdao.delete(pdao.get(id));
        edao.deleteAllEpisodes(id);
        updateListAdapter();

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle("Library");

    }


    public void subscribe(String data) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View promptsView = inflater.inflate(R.layout.subscribe_dialog, null);
        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.rss_feed);
        final ImageButton paste_btn = (ImageButton) promptsView
                .findViewById(R.id.paste_btn);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(promptsView);
        // Set listener for the paste button
        userInput.setText(data);
        userInput.setSelection(data.length());
        paste_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity()
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                String pasteData = null;
                if (clipboard.hasText()) {
                    pasteData = clipboard.getText().toString();
                }
                if (pasteData != null) {
                    userInput.setText(pasteData);
                }
            }
        });
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userString = userInput.getText().toString();
                new BackgroundThread(getActivity()).subscribeToPodcast(
                        userString, (MainActivity) getActivity());
            }
        });

        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.create().show();
    }

    public void updateListAdapter() {
        PodcastAdapter adapter = (PodcastAdapter) getListAdapter();
        adapter.notifyDataSetChanged();
    }

    OnItemClickListener channelListViewOnClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View arg1,
                int itemPosition, long itemId) {

            ((MainActivity) getActivity()).setChannelFragment(itemId);

        }
    };


}
