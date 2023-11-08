package com.elishaazaria.sayboard

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.elishaazaria.sayboard.data.KeepScreenAwakeMode
import com.elishaazaria.sayboard.utils.KeysListSerializer
import com.elishaazaria.sayboard.utils.ModelListSerializer
import com.elishaazaria.sayboard.utils.leftDefaultKeysList
import com.elishaazaria.sayboard.utils.rightDefaultKeysList
import com.elishaazaria.sayboard.utils.topDefaultKeysList
import dev.patrickgold.jetpref.datastore.JetPref
import dev.patrickgold.jetpref.datastore.model.PreferenceModel

// Defining a getter function for easy retrieval of the AppPrefs model.
// You can name this however you want, the convention is <projectName>PreferenceModel
fun sayboardPreferenceModel() = JetPref.getOrCreatePreferenceModel(AppPrefs::class, ::AppPrefs)

// Defining a preference model for our app prefs
// The name we give here is the file name of the preferences and is saved
// within the app's `jetpref_datastore` directory.
class AppPrefs : PreferenceModel("example-app-preferences") {
    val modelsOrder = custom(
        key = "sl_models_order",
        default = listOf(),
        serializer = ModelListSerializer()
    )

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

    val keyboardHeightPortrait = float(
        key = "f_keyboard_height_portrait",
        default = 0.3f
    )

    val keyboardHeightLandscape = float(
        key = "f_keyboard_height_landscape",
        default = 0.5f
    )

    val keyboardKeysTop = custom(
        key = "sl_keyboard_keys_top",
        default = topDefaultKeysList,
        serializer = KeysListSerializer()
    )

    val keyboardKeysLeft = custom(
        key = "sl_keyboard_keys_left",
        default = leftDefaultKeysList,
        serializer = KeysListSerializer()
    )

    val keyboardKeysRight = custom(
        key = "sl_keyboard_keys_right",
        default = rightDefaultKeysList,
        serializer = KeysListSerializer()
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
}