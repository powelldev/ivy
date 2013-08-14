package fireminder.podcastcatcher.db;

import java.util.ArrayList;
import java.util.List;

public enum Playlist {
	instance;
	public List<Episode> episodes = new ArrayList<Episode>();
	int current = 0;
	
	public void addEpisode(Episode newEpisode) {
		episodes.add(newEpisode);
	}
	
	public void removeEpisode(int position) {
		episodes.remove(position);
	}
	
	public void changePosition(int startPos, int endPos){
		
	}
	
	public Episode getCurrent() {
		return null;
	}
	
	public Episode getPrevious() {
		current--;
		if(current < 0){
			current = 0;
		}
		return episodes.get(current);
	}

	public Episode getNext() {
		current++;
		if(current > episodes.size()){
			return null;
		}
		return episodes.get(current);
	}
}
