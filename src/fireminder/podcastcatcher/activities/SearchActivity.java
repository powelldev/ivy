package fireminder.podcastcatcher.activities;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import fireminder.podcastcatcher.OnTaskCompleted;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.utils.Helper;

public class SearchActivity extends ListActivity implements OnTaskCompleted {
	ListView resultsListView;
	EditText search_field_et;
	ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_layout);

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

	/** Launches search */
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
		returnIntent.putExtra("result", url);
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	@Override
	public void onTaskCompleted(final List<String> result) {
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);

		dialog.dismiss();
		if (result == null) {
			Toast.makeText(this, "not found", Toast.LENGTH_LONG).show();
			Intent returnIntent = new Intent();
			setResult(RESULT_CANCELED, returnIntent);
			finish();
		} else if (result.size() > 1) {
			final List<String> list = new ArrayList<String>();
			for (int i = 0; i < result.size(); i++) {
				list.add(result.get(i));
			}
			OnClickListener listListener = new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finishWithItem(list.get(which));
				}
			};
			CharSequence[] cs = list.toArray(new CharSequence[list.size()]);
			mBuilder.setSingleChoiceItems(cs, -1, listListener);

			mBuilder.create().show();
			// finishWithItem(result.get(2));
		} else {
			Toast.makeText(this, "not found", Toast.LENGTH_LONG).show();
			Intent returnIntent = new Intent();
			setResult(RESULT_CANCELED, returnIntent);
			finish();
		}

	}

}
