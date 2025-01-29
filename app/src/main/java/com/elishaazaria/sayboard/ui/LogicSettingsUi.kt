package com.elishaazaria.sayboard.ui

import android.content.Context
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.elishaazaria.sayboard.R
import com.elishaazaria.sayboard.data.KeepScreenAwakeMode
import com.elishaazaria.sayboard.sayboardPreferenceModel
import dev.patrickgold.jetpref.datastore.ui.ListPreference
import dev.patrickgold.jetpref.datastore.ui.ListPreferenceEntry
import dev.patrickgold.jetpref.datastore.ui.ScrollablePreferenceLayout
import dev.patrickgold.jetpref.datastore.ui.SwitchPreference
import dev.patrickgold.jetpref.datastore.ui.listPrefEntries

@Composable
fun getEnabledInputMethodPreferenceEntries(context: Context): List<ListPreferenceEntry<String>> {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val pm = context.packageManager
    return listPrefEntries {
        entry(key = "", label = stringResource(R.string.option_none))

        for (im in imm.enabledInputMethodList) {
            if (im.packageName == context.packageName) continue
            entry(
                key = im.id,
                label = im.loadLabel(pm).toString()
            )
        }
    }
}

@Composable
fun LogicSettingsUi(context: Context) = ScrollablePreferenceLayout(sayboardPreferenceModel()) {
    ListPreference(
        listPref = prefs.logicKeepScreenAwake,
        title = stringResource(id = R.string.logic_keep_screen_awake_title),
        entries = KeepScreenAwakeMode.listEntries()
    )
    SwitchPreference(
        pref = prefs.logicListenImmediately,
        title = stringResource(id = R.string.logic_listen_immediately_title),
        summary = stringResource(
            id = R.string.logic_listen_immediately_summery
        )
    )
    SwitchPreference(
        pref = prefs.logicAutoSwitchBack,
        title = stringResource(id = R.string.logic_auto_switch_back_title),
        summary = stringResource(
            id = R.string.logic_auto_switch_back_summery
        )
    )
    SwitchPreference(
        pref = prefs.logicKeepModelInRam,
        title = stringResource(id = R.string.logic_keep_model_in_ram_title),
        summary = stringResource(
            id = R.string.logic_keep_model_in_ram_summery
        )
    )
    SwitchPreference(
        pref = prefs.logicAutoCapitalize,
        title = stringResource(id = R.string.logic_auto_capitalize_title),
        summary = stringResource(id = R.string.logic_auto_capitalize_summary)
    )

    ListPreference(
        listPref = prefs.logicDefaultIME,
        title = stringResource(id = R.string.logic_default_ime),
        entries = getEnabledInputMethodPreferenceEntries(context)
    )
    SwitchPreference(
        pref = prefs.logicReturnToDefaultIME,
        title = stringResource(id = R.string.logic_return_to_default_ime_title),
        summary = stringResource(id = R.string.logic_return_to_default_ime_summary)
    )
}