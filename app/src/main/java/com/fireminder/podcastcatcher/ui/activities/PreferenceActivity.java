package com.fireminder.podcastcatcher.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;

import com.fireminder.podcastcatcher.R;

public class PreferenceActivity extends ActionBarActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);
    if (savedInstanceState == null) {
      getFragmentManager().beginTransaction()
          .add(R.id.container, new SettingsFragment())
          .commit();
    }
  }

  public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {


    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.preferences);
      SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
      sp.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
      super.onDestroy();
      SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
      sp.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }

  }
}
