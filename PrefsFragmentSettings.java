/***************************************************************
 * David Sta Cruz
 * L20358579
 * COSC 2324
 * Mobile Computer Game Development
 * Dr. Timothy Roden
 ****************************************************************
 * This program will implement the functions of the  high score
 * preference screen for the Bug Masher game.
 ***************************************************************/
package com.example.hm13_stacruz.bugmasher;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class PrefsFragmentSettings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public PrefsFragmentSettings () {
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preference from an XML resource
        addPreferencesFromResource(R.xml.prefs_fragment_settings);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set up a listener when a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        // Dynamically changes the high score displayed in the preference screen whenever it is updated
        Preference prefs = getPreferenceScreen().findPreference("key_highScore");
        String s = "" + Assets.highScore;
        prefs.setSummary(s);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("key_music_enabled")) {
            boolean music_enabled = sharedPreferences.getBoolean("key_music_enabled", true);
            if (!music_enabled) {
                if (Assets.mp != null)
                    Assets.mp.setVolume(0, 0);
            }
            else {
                if (Assets.mp != null) {
                    Assets.mp.setVolume(1, 1);
                }
            }
        }
    }
}
