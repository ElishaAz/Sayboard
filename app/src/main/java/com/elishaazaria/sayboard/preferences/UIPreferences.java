package com.elishaazaria.sayboard.preferences;

import com.elishaazaria.sayboard.AppCtx;
import com.elishaazaria.sayboard.R;

public class UIPreferences {
    public static int getForegroundColor() {
        // TODO: if min-api 26, switch to Color objects
        return MyPreferences.getSharedPref()
                .getInt(AppCtx.getStringRes(R.string.pref_ui_foreground_c),
                        AppCtx.getIntegerRes(R.integer.pref_foreground_color_default));
    }

    public static int getBackgroundColor() {
        // TODO: if min-api 26, switch to Color objects
        return MyPreferences.getSharedPref()
                .getInt(AppCtx.getStringRes(R.string.pref_ui_background_c),
                        AppCtx.getIntegerRes(R.integer.pref_background_color_default));
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
