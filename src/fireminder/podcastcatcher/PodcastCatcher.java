package fireminder.podcastcatcher;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;

public class PodcastCatcher extends Application{
	
	private static PodcastCatcher singleton;
	
	private List<Long> downloadIds;
	
	public static PodcastCatcher getInstance() {
		return singleton;
	}
	
	public void addId(long downloadId){
		downloadIds.add(downloadId);
	}
	
	public void removeId(long id) {
		downloadIds.remove(id);
	}
	
	public List<Long> getDownloadIds(){
		return downloadIds;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		singleton = this;
		downloadIds = new ArrayList<Long>();
	}

}
