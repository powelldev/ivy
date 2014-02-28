package fireminder.podcastcatcher.fragments;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.activities.MainActivity;
import fireminder.podcastcatcher.db.EpisodeDao;
import fireminder.podcastcatcher.db.PodcastDao;
import fireminder.podcastcatcher.downloads.BackgroundThread;
import fireminder.podcastcatcher.ui.PodcastAdapter;
import fireminder.podcastcatcher.utils.RssParser;
import fireminder.podcastcatcher.utils.Utils;
import fireminder.podcastcatcher.valueobjects.Podcast;

public class PodcastFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{
    
    private static final int mLoaderId = 0;

    final static String TAG = PodcastFragment.class.getSimpleName();

    public PodcastAdapter cursorAdapter;
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initialize();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        View rootView = inflater.inflate(R.layout.listfragment1, container, false);
        
        PodcastDao pdao = new PodcastDao(getActivity());
        Cursor podcastCursor = pdao.getAllPodcastsAsCursor();
        cursorAdapter = new PodcastAdapter(getActivity(), podcastCursor, 0);
        setListAdapter(cursorAdapter);

        subscribeIfIntent();

        return rootView;
    }

    private void subscribeIfIntent(){
        try {
            Bundle b = this.getArguments();
            subscribe(b.getString("uri"));
        } catch (Exception e) {}
    }
    
    public void subscribe(String data){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        
        View promptsView = inflater.inflate(R.layout.subscribe_dialog, null);
        final EditText userInput = (EditText) promptsView.findViewById(R.id.rss_feed);
        final ImageButton paste_btn = (ImageButton) promptsView.findViewById(R.id.paste_btn);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(promptsView);        
        //Set listener for the paste button
        userInput.setText(data);
        userInput.setSelection(data.length());
        paste_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                String pasteData = null;
                if(clipboard.hasText()){
                    pasteData = clipboard.getText().toString();
                }
                if(pasteData!=null){
                    userInput.setText(pasteData);
                }
            }
        });
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userString = userInput.getText().toString();
                new BackgroundThread(getActivity()).subscribeToPodcast(userString, (MainActivity) getActivity());
                //new HttpDownloadTask().execute(userString);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        
        builder.create().show();
    }
    private OnClickListener subscribeClickListener = new OnClickListener(){
        @Override
        public void onClick(View button) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            
            View promptsView = inflater.inflate(R.layout.subscribe_dialog, null);
            final EditText userInput = (EditText) promptsView.findViewById(R.id.rss_feed);
            userInput.setSelection(7);
            final ImageButton paste_btn = (ImageButton) promptsView.findViewById(R.id.paste_btn);
            
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(promptsView);        

            paste_btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    String pasteData = null;
                    if(clipboard.hasText()){
                        pasteData = clipboard.getText().toString();
                    }
                    if(pasteData!=null){
                        userInput.setText(pasteData);
                    }
                }
            });

            //Get user URL, parse it, update ListView, update database
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String userString = userInput.getText().toString();
                    //BackgroundThread bt = new BackgroundThread(getActivity());
                    //bt.getPodcastInfoFromBackgroundThread(userString);
                    if(Utils.isHTTPAvailable()) {    
                        new BackgroundThread(getActivity()).subscribeToPodcast(userString, (MainActivity) getActivity());
                        //new HttpDownloadTask().execute(userString);
                    }
                    else {
                        Toast.makeText(getActivity(), "Connection Unavaliable", Toast.LENGTH_LONG).show();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            
            builder.create().show();
        }
    };
    
    public void updateListAdapter(){
        try {
        cursorAdapter.notifyDataSetChanged();
        } catch (Exception e) {}
    }
    

    private class HttpDownloadTask extends AsyncTask<String, Void, Podcast>{
        PodcastDao pdao = new PodcastDao(getActivity());
        long id;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            id = pdao.insert(new Podcast("Loading ..."));
            updateListAdapter();
        }

        @Override
        protected Podcast doInBackground(String... urls) {
            
            BufferedReader reader = null;
            Podcast podcast = null;
            try{
                URL url = new URL(urls[0]);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                InputStream is = con.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is));
                podcast= RssParser.parsePodcastFromXml(is);
                podcast.setLink(urls[0]);
            } 
            catch(MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                return null;
            }
            return podcast;
        }
        
        @Override
        protected void onPostExecute(Podcast result){
            pdao.delete(pdao.get(id));
            if(result != null){
                Podcast podcast = pdao.get(pdao.insert(result));
                BackgroundThread bt = new BackgroundThread(getActivity());
                bt.getEpisodesFromBackgroundThread(podcast);
                Log.d(TAG, "parsing for episodes");
            }
            else{
                Toast.makeText(getActivity(), "Podcast subscription failed: Please check url", Toast.LENGTH_LONG).show();
            }
            updateListAdapter();
        }
    }
    
    OnItemClickListener channelListViewOnClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View arg1, int itemPosition,
                long itemId) {

            ((MainActivity) getActivity()).setChannelFragment(itemId);

            
        }
    };
    OnItemLongClickListener channelListViewOnItemLongClickListener = 
            new OnItemLongClickListener(){
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                int itemPosition, long itemId) {
            PodcastDao pdao = new PodcastDao(getActivity());
            EpisodeDao edao = new EpisodeDao(getActivity());
            pdao.delete(pdao.get(itemId));
            edao.deleteAllEpisodes(itemId);
            updateListAdapter();
            return false;
        }
    };
    
    private void initialize() {
        ListView listView = getListView();
        listView.setOnItemClickListener(channelListViewOnClickListener);
        listView.setOnItemLongClickListener(channelListViewOnItemLongClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        // TODO Auto-generated method stub
        
    }
    
}
