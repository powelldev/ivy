package fireminder.podcastcatcher.fragments;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.utils.Utils;


public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preference, String key) {
        Log.e(Utils.TAG, "KEY CHANGED AMG");
        /*
        AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), ADownloadService.class);
        PendingIntent pi = PendingIntent.getService(getActivity(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.set(AlarmManager.RTC, System.currentTimeMillis(), pi);
        */
    }
}
