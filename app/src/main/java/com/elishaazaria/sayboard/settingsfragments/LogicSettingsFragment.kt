package com.elishaazaria.sayboard.settingsfragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.elishaazaria.sayboard.AppCtx.getStringRes
import com.elishaazaria.sayboard.R

class LogicSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val preferenceManager = preferenceManager
        preferenceManager.sharedPreferencesName = getStringRes(R.string.main_shared_pref)
        setPreferencesFromResource(R.xml.logic_preferences, rootKey)
    }
}