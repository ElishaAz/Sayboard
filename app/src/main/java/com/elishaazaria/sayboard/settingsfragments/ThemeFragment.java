package com.elishaazaria.sayboard.settingsfragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.elishaazaria.sayboard.R;

public class ThemeFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.theme_preferences, rootKey);
    }
}