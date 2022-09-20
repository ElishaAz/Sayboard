package org.vosk.ime.settingsfragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import org.vosk.ime.R;

public class ThemeFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.theme_preferences, rootKey);
    }
}