package fireminder.podcastcatcher.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class PodcastProvider extends ContentProvider {
    
    private SqlHelper mSqlHelper;
    
    private static final int PODCASTS = 10;
    private static final int EPISODES = 30;

    private static final int PODCASTS_ID = 20;
    private static final int EPISODES_ID = 40;
    
    private static final String AUTHORITY = "fireminder.podcastcatcher.db.provider";
    
    private static final String BASE_PATH_POD = "podcasts";
    private static final String BASE_PATH_EPI = "episodes";
    
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, BASE_PATH_POD, PODCASTS);
        sUriMatcher.addURI(AUTHORITY, BASE_PATH_EPI, EPISODES);
        sUriMatcher.addURI(AUTHORITY, BASE_PATH_POD + "/#", PODCASTS_ID);
        sUriMatcher.addURI(AUTHORITY, BASE_PATH_EPI + "/#", EPISODES_ID);
    }
            
    @Override
    public boolean onCreate() {
        mSqlHelper = SqlHelper.getInstance(getContext());
        return false;
    }

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getType(Uri arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri arg0, ContentValues arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] args,
            String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        
        int uriType = sUriMatcher.match(uri);
        
        switch (uriType) {
        case EPISODES_ID:
            queryBuilder.appendWhere(EpisodeDao.COLUMN_ID + "="
                    + uri.getLastPathSegment());
            break;
        case PODCASTS_ID:
            queryBuilder.appendWhere(PodcastDao.COLUMN_ID + "="
                    + uri.getLastPathSegment());
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        
        
        return null;
    }

    @Override
    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
        // TODO Auto-generated method stub
        return 0;
    }

}
