package fireminder.podcastcatcher.activities;

import android.app.Fragment;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.db.EpisodeDao2;
import fireminder.podcastcatcher.db.PodcastDao2;

public class ChannelFragment extends ListFragment{

    private final static String TAG = ChannelFragment.class.getSimpleName();

    EpisodeDao2 mEdao = new EpisodeDao2();
    PodcastDao2 mPdao = new PodcastDao2();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = (View) inflater.inflate(R.layout.channel, container, false);

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
