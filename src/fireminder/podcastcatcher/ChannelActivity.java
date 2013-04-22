package fireminder.podcastcatcher;

import java.io.ByteArrayInputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import fireminder.podcastcatcher.db.Podcast;
import fireminder.podcastcatcher.db.PodcastDAO;

public class ChannelActivity extends Activity{

	@Override
	protected void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.channel);
		TextView tv = (TextView) findViewById(R.id.textview);
		TextView descriptv = (TextView) findViewById(R.id.podcast_descrip);
		ImageView iv = (ImageView) findViewById(R.id.podcast_image);
		
		Intent intent = getIntent();
		long id = intent.getLongExtra("channel_id", 0);
		Log.d("ChannelActivity id: ", "" + id);
		
		PodcastDAO podcastDao = new PodcastDAO(this);
		podcastDao.open();
		Podcast podcast = podcastDao.getPodcast(id);
		
		tv.setText(podcast.getTitle());
		descriptv.setText(podcast.getDescription());
		try{
		ByteArrayInputStream is = new ByteArrayInputStream(podcast.getImagelink());
		Bitmap image = BitmapFactory.decodeStream(is);
		iv.setImageBitmap(image);
		} 
		catch (Exception e){
			iv.setImageResource(R.drawable.ic_launcher);
			e.printStackTrace();
			}
		
		
	}
}
