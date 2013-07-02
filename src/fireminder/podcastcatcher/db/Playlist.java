package fireminder.podcastcatcher.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;

public class Playlist {

	public List<Integer> episode_ids;
	public List<Integer> positions;

	Playlist(List<Integer> ids, List<Integer> positions){
		this.episode_ids = ids;
		this.positions = positions;
	}
	
	public Playlist(Cursor cursor){
		this.episode_ids = new ArrayList<Integer>();
		this.positions = new ArrayList<Integer>();
		while(cursor.moveToNext()){
			episode_ids.add(cursor.getInt(cursor.getColumnIndex(PlaylistSqlHelper.COLUMN_EPISODE_ID)));
			positions.add(cursor.getInt(cursor.getColumnIndex(PlaylistSqlHelper.COLUMN_POSITION)));
		}
	}
	
	public ArrayList<String> getEpisodeTitles(Context context){
		EpisodeDAO edao = new EpisodeDAO(context);
		edao.open();
		ArrayList<String> episodes = new ArrayList<String>();
		for(Integer id : episode_ids){
			episodes.add(edao.getEpisode(id).getTitle());
		}
		edao.close();
		return  episodes;
	}
	public List<Episode> getEpisodes(Context context){
		EpisodeDAO edao = new EpisodeDAO(context);
		List<Episode> episodes = new ArrayList<Episode>();
		for(Integer id : episode_ids){
			episodes.add(edao.getEpisode(id));
		}
		return episodes;
	}
}
