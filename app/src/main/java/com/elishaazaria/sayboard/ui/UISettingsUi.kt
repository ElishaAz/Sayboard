package com.elishaazaria.sayboard.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.elishaazaria.sayboard.R
import com.elishaazaria.sayboard.sayboardPreferenceModel
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.datastore.ui.DialogSliderPreference
import dev.patrickgold.jetpref.datastore.ui.ExperimentalJetPrefDatastoreUi
import dev.patrickgold.jetpref.datastore.ui.PreferenceGroup
import dev.patrickgold.jetpref.datastore.ui.ScrollablePreferenceLayout
import dev.patrickgold.jetpref.datastore.ui.SwitchPreference

@OptIn(ExperimentalJetPrefDatastoreUi::class)
@Composable
fun UISettingsUi() = ScrollablePreferenceLayout(sayboardPreferenceModel()) {
    PreferenceGroup(title = stringResource(id = R.string.ui_light_theme_header)) {
        SwitchPreference(
            pref = prefs.uiDayForegroundMaterialYou,
            title = stringResource(id = R.string.ui_light_theme_foreground_material_you_title)
        )

        ColorPickerPreference(
            pref = prefs.uiDayForeground,
            title = stringResource(id = R.string.ui_light_theme_foreground_color_title),
            enabled = !prefs.uiDayForegroundMaterialYou.observeAsState().value
        )

        ColorPickerPreference(
            pref = prefs.uiDayBackground,
            title = stringResource(id = R.string.ui_light_theme_background_color_title)
        )
    }
    PreferenceGroup(title = stringResource(id = R.string.ui_dark_theme_header)) {
        SwitchPreference(
            pref = prefs.uiNightForegroundMaterialYou,
            title = stringResource(id = R.string.ui_dark_theme_foreground_material_you_title)
        )

        ColorPickerPreference(
            pref = prefs.uiNightForeground,
            title = stringResource(id = R.string.ui_dark_theme_foreground_color_title),
            enabled = !prefs.uiNightForegroundMaterialYou.observeAsState().value
        )

        ColorPickerPreference(
            pref = prefs.uiNightBackground,
            title = stringResource(id = R.string.ui_dark_theme_background_color_title)
        )
    }
    PreferenceGroup(title = stringResource(id = R.string.ui_keyboard_header)) {
        DialogSliderPreference(
            pref = prefs.uiKeyboardHeightPortrait,
            title = stringResource(id = R.string.other_keyboard_height_portrait_title),
            min = 0.01f,
            max = 1f,
            stepIncrement = 0.01f
        )
        DialogSliderPreference(
            pref = prefs.uiKeyboardHeightLandscape,
            title = stringResource(id = R.string.other_keyboard_height_landscape_title),
            min = 0.01f,
            max = 1f,
            stepIncrement = 0.01f
        )
    }
}