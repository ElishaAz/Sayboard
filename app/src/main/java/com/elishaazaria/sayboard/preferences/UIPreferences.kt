package com.elishaazaria.sayboard.preferences

import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import com.elishaazaria.sayboard.AppCtx.getBoolRes
import com.elishaazaria.sayboard.AppCtx.getIntegerRes
import com.elishaazaria.sayboard.AppCtx.getStringRes
import com.elishaazaria.sayboard.R

object UIPreferences {
    fun isForegroundMaterialYou(dark: Boolean): Boolean {
        return if (!dark) {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && MyPreferences.sharedPref
                .getBoolean(
                    getStringRes(R.string.pref_ui_light_foreground_material_you_b),
                    getBoolRes(R.bool.pref_light_foreground_material_you_default)
                )
        } else {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && MyPreferences.sharedPref
                .getBoolean(
                    getStringRes(R.string.pref_ui_dark_foreground_material_you),
                    getBoolRes(R.bool.pref_dark_foreground_material_you_default)
                )
        }
    }

    @JvmStatic
    fun getForegroundColor(dark: Boolean, ctx: Context?): Int {
        if (isForegroundMaterialYou(dark)) {
            return ContextCompat.getColor(ctx!!, R.color.materialYouForeground)
        }
        return if (!dark) {
            MyPreferences.sharedPref
                .getInt(
                    getStringRes(R.string.pref_ui_light_foreground_c),
                    getIntegerRes(R.integer.pref_light_foreground_color_default)
                )
        } else {
            MyPreferences.sharedPref
                .getInt(
                    getStringRes(R.string.pref_ui_dark_foreground_c),
                    getIntegerRes(R.integer.pref_dark_foreground_color_default)
                )
        }
    }

    @JvmStatic
    fun getBackgroundColor(dark: Boolean): Int {
        return if (!dark) {
            MyPreferences.sharedPref
                .getInt(
                    getStringRes(R.string.pref_ui_light_background_c),
                    getIntegerRes(R.integer.pref_light_background_color_default)
                )
        } else {
            MyPreferences.sharedPref
                .getInt(
                    getStringRes(R.string.pref_ui_dark_background_c),
                    getIntegerRes(R.integer.pref_dark_background_color_default)
                )
        }
    }

    @JvmStatic
    val screenHeightLandscape: Float
        get() = MyPreferences.sharedPref.getInt(
            getStringRes(R.string.pref_ui_keyboard_height_landscape_i),
            getIntegerRes(R.integer.pref_keyboard_height_landscape_default)
        ) / getIntegerRes(R.integer.keyboard_height_max).toFloat()
    @JvmStatic
    val screenHeightPortrait: Float
        get() = MyPreferences.sharedPref.getInt(
            getStringRes(R.string.pref_ui_keyboard_height_portrait_i),
            getIntegerRes(R.integer.pref_keyboard_height_portrait_default)
        ) / getIntegerRes(R.integer.keyboard_height_max).toFloat()
}