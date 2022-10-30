package com.elishaazaria.sayboard.settingsfragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.elishaazaria.sayboard.AppCtx;
import com.elishaazaria.sayboard.R;

public class LogicSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(AppCtx.getStringRes(R.string.main_shared_pref));
        setPreferencesFromResource(R.xml.logic_preferences, rootKey);
    }
}