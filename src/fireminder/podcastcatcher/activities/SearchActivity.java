package fireminder.podcastcatcher.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import fireminder.podcastcatcher.R;

public class SearchActivity extends ListActivity implements Searcher.OnSearchComplete {

    private ListView resultsListView;
    private EditText search_field_et;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        search_field_et = (EditText) findViewById(R.id.search_field_et);
        resultsListView = getListView();

        setClickListeners();
    }

    private void setClickListeners() {

        Button searchBtn = (Button) findViewById(R.id.search_btn);
        searchBtn.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });

        resultsListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                finish();
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

    public void search() {
        showSearchingDialog();
        Searcher searcher = new Searcher(this, this);
        String searchTerm = search_field_et.getEditableText().toString();
        searcher.search(searchTerm);
    }

    private void showSearchingDialog() {
        this.dialog = ProgressDialog.show(this, "Searching", "Please wait...", true);
    }

    public void finishWithItems() {
        String[] podcasts = new String[mSelectedPodcasts.size()];
        for (int i = 0; i < mSelectedPodcasts.size(); i++) {
            podcasts[i] = mSelectedPodcasts.get(i);
            Log.d("PodcastCatcher", "Loading podcast " + podcasts[i]);
        }
        Bundle b = new Bundle();
        b.putStringArray("result", podcasts);
        Intent data = new Intent();
        data.putExtras(b);
        setResult(RESULT_OK, data);
        finish();
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

    @Override
    public void searchComplete(List<String> results) {
        dismissMyDialog();
        mSearchResultsList = results;
        setupSelectionDialog(results);
    }

    private void dismissMyDialog() {
        try {
            dialog.dismiss();
        } catch (Exception e) {
            // fall through
        }
    }

    private void setupSelectionDialog(List<String> results) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        CharSequence[] cs = mSearchResultsList.toArray(new CharSequence[mSearchResultsList.size()]);
        builder.setMultiChoiceItems(cs, null, mSearchResultListListener);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishWithItems();
            }
        });
        builder.create().show();

    }

    @Override
    public void searchFailure() {
        dismissMyDialog();
        Toast.makeText(this, R.string.search_not_found, Toast.LENGTH_LONG).show();
    }
}
