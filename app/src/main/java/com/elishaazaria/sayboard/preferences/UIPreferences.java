package com.elishaazaria.sayboard.preferences;

import android.content.Context;
import android.os.Build;

import androidx.core.content.ContextCompat;

import com.elishaazaria.sayboard.AppCtx;
import com.elishaazaria.sayboard.R;

public class UIPreferences {
    public static boolean isForegroundMaterialYou(boolean dark) {
        if (!dark) {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && MyPreferences.getSharedPref()
                    .getBoolean(AppCtx.getStringRes(R.string.pref_ui_light_foreground_material_you_b),
                            AppCtx.getBoolRes(R.bool.pref_light_foreground_material_you_default));
        } else {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && MyPreferences.getSharedPref()
                    .getBoolean(AppCtx.getStringRes(R.string.pref_ui_dark_foreground_material_you),
                            AppCtx.getBoolRes(R.bool.pref_dark_foreground_material_you_default));
        }
    }

    public static int getForegroundColor(boolean dark, Context ctx) {
        if (isForegroundMaterialYou(dark)) {
            return ContextCompat.getColor(ctx, R.color.materialYouForeground);
        }

        if (!dark) {
            return MyPreferences.getSharedPref()
                    .getInt(AppCtx.getStringRes(R.string.pref_ui_light_foreground_c),
                            AppCtx.getIntegerRes(R.integer.pref_light_foreground_color_default));
        } else {
            return MyPreferences.getSharedPref()
                    .getInt(AppCtx.getStringRes(R.string.pref_ui_dark_foreground_c),
                            AppCtx.getIntegerRes(R.integer.pref_dark_foreground_color_default));
        }
    }

    public static int getBackgroundColor(boolean dark) {
        if (!dark) {
            return MyPreferences.getSharedPref()
                    .getInt(AppCtx.getStringRes(R.string.pref_ui_light_background_c),
                            AppCtx.getIntegerRes(R.integer.pref_light_background_color_default));
        } else {
            return MyPreferences.getSharedPref()
                    .getInt(AppCtx.getStringRes(R.string.pref_ui_dark_background_c),
                            AppCtx.getIntegerRes(R.integer.pref_dark_background_color_default));
        }
    }

    public static float getScreenHeightLandscape() {
        return MyPreferences.getSharedPref().getInt(AppCtx.getStringRes(R.string.pref_ui_keyboard_height_landscape_i),
                AppCtx.getIntegerRes(R.integer.pref_keyboard_height_landscape_default)) / (float) AppCtx.getIntegerRes(R.integer.keyboard_height_max);
    }

    public static float getScreenHeightPortrait() {
        return MyPreferences.getSharedPref().getInt(AppCtx.getStringRes(R.string.pref_ui_keyboard_height_portrait_i),
                AppCtx.getIntegerRes(R.integer.pref_keyboard_height_portrait_default)) / (float) AppCtx.getIntegerRes(R.integer.keyboard_height_max);
    }
}
