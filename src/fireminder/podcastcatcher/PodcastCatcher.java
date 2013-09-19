package fireminder.podcastcatcher;

import android.app.Application;
import android.content.Context;

public class PodcastCatcher {
	
	private static PodcastCatcher singleton;
	private Context context;
	
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
	
	private PodcastCatcher() {
		
	}

}
