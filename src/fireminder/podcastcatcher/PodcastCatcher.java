package fireminder.podcastcatcher;

import android.content.Context;
import fireminder.podcastcatcher.activities.MainActivity;

public class PodcastCatcher {
	
	private static PodcastCatcher singleton;
	private Context context;
	private MainActivity mActivity;
	
	public static PodcastCatcher getInstance() {
		if(singleton == null) {
			singleton = new PodcastCatcher();
		}
		return singleton;
	}
	
	public void setContext(Context context){
		this.context = context;
	}
	
	public Context getContext(){
		return context;
	}
	
	public void setActivity(MainActivity activity) {
		this.mActivity = activity;
	}
	
	private PodcastCatcher() {
		
	}

	public OnTaskCompleted getMainActivity() {
		return mActivity;
	}

}
