package fireminder.podcastcatcher;

import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class SearchActivity extends ListActivity implements OnTaskCompleted{
	ListView resultsListView;
	EditText search_field_et;
	ProgressDialog dialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_layout);
		
		search_field_et = (EditText) findViewById(R.id.search_field_et);
		resultsListView = getListView();
		resultsListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				finishWithItem(null);
			}
			
		});
	}
	
	public void search(View v){
		String s = search_field_et.getEditableText().toString();
		if(!s.matches("")){
			dialog = ProgressDialog.show(this, "Searching", "Please wait...", true);
			Helper.searchForPodcasts(this, s);
		}
	}
	public void finishWithItem(String url){

		Intent returnIntent = new Intent();
		returnIntent.putExtra("result", url);
		setResult(RESULT_OK, returnIntent);
		finish();
	}

	@Override
	public void onTaskCompleted(List<String> result) {

		dialog.dismiss();
		if(result.size() > 1){
			finishWithItem(result.get(2));
		} else {
			Toast.makeText(this, "not found", Toast.LENGTH_LONG).show();
		}
		
	}

}
