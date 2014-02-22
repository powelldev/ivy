package fireminder.podcastcatcher.activities;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import fireminder.podcastcatcher.OnTaskCompleted;
import fireminder.podcastcatcher.PodcastCatcher;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.downloads.BackgroundThread;
import fireminder.podcastcatcher.utils.Helper;

public class SearchActivity extends ListActivity implements OnTaskCompleted {

    private ListView resultsListView;
    private EditText search_field_et;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);

        Button searchBtn = (Button) findViewById(R.id.search_btn);
        searchBtn.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search(v);
            }
        });

        search_field_et = (EditText) findViewById(R.id.search_field_et);
        resultsListView = getListView();
        resultsListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                finishWithItem(null);
            }

        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    public void search(View v) {
        String rawSearchTerm = search_field_et.getEditableText().toString();
        // Sanitize string
        if (!rawSearchTerm.matches("")) {
            try {
                rawSearchTerm = URLEncoder.encode(rawSearchTerm, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            dialog = ProgressDialog.show(this, "Searching", "Please wait...",
                    true);
            Helper.searchForPodcasts(this, rawSearchTerm);
        }
    }

    public void finishWithItem(String url) {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onTaskCompleted(final List<String> result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        try {
        dialog.dismiss();
        } catch (Exception e) {}
        if (result == null) {
            Toast.makeText(this, R.string.search_not_found, Toast.LENGTH_LONG)
                    .show();
        } else if (result.size() > 1) {
            mSearchResultsList = result;
            CharSequence[] cs = mSearchResultsList
                    .toArray(new CharSequence[mSearchResultsList.size()]);
            builder.setMultiChoiceItems(cs, null, mSearchResultListListener);
            builder.setPositiveButton(R.string.ok,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BackgroundThread bt = new BackgroundThread(PodcastCatcher.getInstance().getContext());
                            for (String podcast : mSelectedPodcasts) {
                                bt.subscribeToPodcast(podcast);
                            }
                            finishWithItem(null);
                        }
                    });
            builder.create().show();
            // finishWithItem(result.get(2));
        } else {
            Toast.makeText(this, R.string.search_not_found, Toast.LENGTH_LONG)
                    .show();
        }

    }

    private List<String> mSearchResultsList;
    private List<String> mSelectedPodcasts = new ArrayList<String>();

    private DialogInterface.OnMultiChoiceClickListener mSearchResultListListener = new DialogInterface.OnMultiChoiceClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            String result = mSearchResultsList.get(which);
            if (isChecked) {
                mSelectedPodcasts.add(result);
            } else {
                mSelectedPodcasts.remove(result);
            }
        }
    };
}
