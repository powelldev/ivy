package fireminder.podcastcatcher.db;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class PlaylistDAO {

    private PlaylistSqlHelper dbHelper;
    private SQLiteDatabase db;
public static int loc = 0;
    public PlaylistDAO(Context context) {
        dbHelper = new PlaylistSqlHelper(context);
    }
    
    public void open(){
    	db = dbHelper.getWritableDatabase();
    }
    public void close(){
    	db.close();
    }
    
    public void deleteAll(){
    	db.delete(PlaylistSqlHelper.TABLE_NAME, null, null);
    }
    public void addEpisode(long episode_id){
    	ContentValues values = new ContentValues();
    	values.put(PlaylistSqlHelper.COLUMN_EPISODE_ID, episode_id);
    	values.put(PlaylistSqlHelper.COLUMN_POSITION, loc++);
    	db.insert(PlaylistSqlHelper.TABLE_NAME, null, values);
    	
    }

    public Cursor getPlaylist() {
        List<Episode> playlist = null;
        EpisodeDAO edao = null;

        Cursor cursor = db.query(PlaylistSqlHelper.TABLE_NAME,
                PlaylistSqlHelper.allColumns,
                null, null, null, null, null);
        
        return cursor;

    }


}
