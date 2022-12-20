package com.elishaazaria.sayboard.settingsfragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.elishaazaria.sayboard.AppCtx;
import com.elishaazaria.sayboard.R;
import com.rarepebble.colorpicker.ColorPreference;

public class UIFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(AppCtx.getStringRes(R.string.main_shared_pref));
        setPreferencesFromResource(R.xml.ui_preferences, rootKey);
    }

    @Override
    public void onDisplayPreferenceDialog(@NonNull Preference preference) {
        if (preference instanceof ColorPreference) {
            ((ColorPreference) preference).showDialog(this, 0);
        } else super.onDisplayPreferenceDialog(preference);
    }
}