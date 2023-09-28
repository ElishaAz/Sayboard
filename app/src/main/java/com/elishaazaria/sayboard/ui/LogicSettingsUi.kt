package com.elishaazaria.sayboard.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.elishaazaria.sayboard.R
import com.elishaazaria.sayboard.data.KeepScreenAwakeMode
import com.elishaazaria.sayboard.sayboardPreferenceModel
import dev.patrickgold.jetpref.datastore.ui.ListPreference
import dev.patrickgold.jetpref.datastore.ui.ScrollablePreferenceLayout
import dev.patrickgold.jetpref.datastore.ui.SwitchPreference

@Composable
fun LogicSettingsUi() = ScrollablePreferenceLayout(sayboardPreferenceModel()) {
    ListPreference(
        listPref = prefs.logicKeepScreenAwake,
        title = stringResource(id = R.string.other_keep_screen_awake_title),
        entries = KeepScreenAwakeMode.listEntries()
    )
    SwitchPreference(
        pref = prefs.logicListenImmediately,
        title = stringResource(id = R.string.other_listen_immediately_title),
        summary = stringResource(
            id = R.string.other_listen_immediately_summery
        )
    )
    SwitchPreference(
        pref = prefs.logicAutoSwitchBack,
        title = stringResource(id = R.string.other_auto_switch_back_title),
        summary = stringResource(
            id = R.string.other_auto_switch_back_summery
        )
    )
    SwitchPreference(
        pref = prefs.logicWeakRefToModel,
        title = stringResource(id = R.string.other_weak_ref_to_model_title),
        summary = stringResource(
            id = R.string.other_weak_ref_to_model_summery
        )
    )
}