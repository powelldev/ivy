package fireminder.podcastcatcher.fragments;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.View;
import fireminder.podcastcatcher.R;
import fireminder.podcastcatcher.activities.MainActivity;

public class SettingsFragment extends PreferenceFragment implements
        OnSharedPreferenceChangeListener {
    public static final String FLAG_AUTO_DELETE = "auto_delete";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        getActivity().getActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(255, 86, 116, 185)));
        super.onViewCreated(view, savedInstanceState);
    }
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preference,
            String key) {
        ((MainActivity) getActivity()).onSharedPreferenceChanged(preference,
                key);
    }
}
