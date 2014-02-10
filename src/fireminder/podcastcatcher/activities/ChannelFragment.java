package fireminder.podcastcatcher.activities;

import java.io.ByteArrayInputStream;

import android.app.ListFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.db.EpisodeDao2;
import fireminder.podcastcatcher.db.PodcastDao2;
import fireminder.podcastcatcher.valueobjects.Podcast;

public class ChannelFragment extends ListFragment{

    private final static String TAG = ChannelFragment.class.getSimpleName();

    EpisodeDao2 mEdao = new EpisodeDao2();
    PodcastDao2 mPdao = new PodcastDao2();

    public static ChannelFragment newInstance(int channelId) {
    	ChannelFragment channelFragment = new ChannelFragment();
    	Bundle args = new Bundle();
    	args.putInt("channel_id", channelId);
    	channelFragment.setArguments(args);
    	return channelFragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	int channelId;
    	Podcast podcast;
    	Bitmap image;

        View rootView = (View) inflater.inflate(R.layout.channel, container, false);
        
        channelId = getArguments().getInt("channel_id");
        
        podcast = mPdao.get(channelId);
        
        try {
        	ByteArrayInputStream is = new ByteArrayInputStream(podcast.getImagePath());
        	image = BitmapFactory.decodeStream(is);
        } catch (Exception e) {
        	image = null;
        }
        
        ((TextView) rootView.findViewById(R.id.title_tv)).setText(podcast.getTitle());
        if (image != null)
        	((ImageView) rootView.findViewById(R.id.podcast_image)).setImageBitmap(image);
        else 
        	((ImageView) rootView.findViewById(R.id.podcast_image)).setImageResource(R.drawable.ic_launcher);

        return super.onCreateView(inflater, container, savedInstanceState);
    }
    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }
    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }


}
