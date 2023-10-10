package com.elishaazaria.sayboard

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.elishaazaria.sayboard.data.KeepScreenAwakeMode
import dev.patrickgold.jetpref.datastore.JetPref
import dev.patrickgold.jetpref.datastore.model.PreferenceModel

// Defining a getter function for easy retrieval of the AppPrefs model.
// You can name this however you want, the convention is <projectName>PreferenceModel
fun sayboardPreferenceModel() = JetPref.getOrCreatePreferenceModel(AppPrefs::class, ::AppPrefs)

// Defining a preference model for our app prefs
// The name we give here is the file name of the preferences and is saved
// within the app's `jetpref_datastore` directory.
class AppPrefs : PreferenceModel("example-app-preferences") {
    val logicKeepScreenAwake = enum(
        key = "e_keep_screen_awake",
        default = KeepScreenAwakeMode.NEVER
    )

    val logicListenImmediately = boolean(
        key = "b_listen_immediately",
        default = false
    )

    val logicAutoSwitchBack = boolean(
        key = "b_auto_switch_back_ime",
        default = false
    )

    val logicKeepModelInRam = boolean(
        key = "b_keep_model_in_ram",
        default = false
    )

    val uiDayForegroundMaterialYou = boolean(
        key = "b_day_foreground_material_you",
        default = false
    )
    val uiDayForeground = int(
        key = "c_day_foreground_color",
        default = Color(0xFF377A00).toArgb()
    )
    val uiDayBackground = int(
        key = "c_day_background_color",
        default = Color(0xFFFFFFFF).toArgb()
    )

    val uiNightForegroundMaterialYou = boolean(
        key = "b_night_foreground_material_you",
        default = false
    )
    val uiNightForeground = int(
        key = "c_night_foreground_color",
        default = Color(0xFF66BB6A).toArgb()
    )
    val uiNightBackground = int(
        key = "c_night_background_color",
        default = Color(0xFF000000).toArgb()
    )

    val uiKeyboardHeightPortrait = float(
        key = "f_keyboard_height_portrait",
        default = 0.3f
    )

    val uiKeyboardHeightLandscape = float(
        key = "f_keyboard_height_landscape",
        default = 0.5f
    )


    val showExampleGroup = boolean(
        key = "show_example_group",
        default = true,
    )
    val boxSizePortrait = int(
        key = "box_size_portrait",
        default = 40,
    )
    val boxSizeLandscape = int(
        key = "box_size_landscape",
        default = 20,
    )
    val welcomeMessage = string(
        key = "welcome_message",
        default = "Hello world!",
    )
}