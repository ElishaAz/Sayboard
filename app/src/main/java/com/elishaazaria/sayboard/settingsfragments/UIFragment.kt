package com.elishaazaria.sayboard.settingsfragments

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.elishaazaria.sayboard.AppCtx.getStringRes
import com.elishaazaria.sayboard.R
import com.rarepebble.colorpicker.ColorPreference

class UIFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val preferenceManager = preferenceManager
        preferenceManager.sharedPreferencesName = getStringRes(R.string.main_shared_pref)
        setPreferencesFromResource(R.xml.ui_preferences, rootKey)
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is ColorPreference) {
            preference.showDialog(this, 0)
        } else super.onDisplayPreferenceDialog(preference)
    }
}