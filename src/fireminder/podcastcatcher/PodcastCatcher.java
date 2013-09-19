package fireminder.podcastcatcher;

import android.app.Application;

public class PodcastCatcher extends Application{
	
	private static PodcastCatcher singleton;
	
	public static PodcastCatcher getInstance() {
		return singleton;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		singleton = this;
	}

}
