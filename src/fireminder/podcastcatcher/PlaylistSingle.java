package fireminder.podcastcatcher;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import fireminder.podcastcatcher.activities.MainActivity;
import fireminder.podcastcatcher.db.Episode;

public enum PlaylistSingle {
	instance;
	
	public List<Episode> songList = new ArrayList<Episode>();
	private Episode current;
	
	public Service service = null;
	public MainActivity frontEnd = null;
	
	public synchronized Episode getCurrent(){
		if(current == null){
			current = songList.get(0);
		}
		return current;
	}
	
	public synchronized Episode nextSong() {
		current = null;
		
		if(!songList.isEmpty()) {
			current = songList.remove(0);
		}
		
		if(frontEnd != null) {
			frontEnd.changeSong(current);
		}
		
		return current;
	}

}
