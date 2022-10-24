package com.elishaazaria.sayboard.preferences;

import android.graphics.Color;
import android.os.Build;

import com.elishaazaria.sayboard.AppCtx;
import com.elishaazaria.sayboard.R;

public class ThemePreferences {
    public static int getForegroundColor() {
        // TODO: if min-api 26, switch to Color objects
        return MyPreferences.getSharedPref()
                .getInt(AppCtx.getStringRes(R.string.pref_theme_foreground_c),
                        AppCtx.getIntegerRes(R.integer.pref_foreground_color_default));
    }

    public static int getBackgroundColor() {
        // TODO: if min-api 26, switch to Color objects
        return MyPreferences.getSharedPref()
                .getInt(AppCtx.getStringRes(R.string.pref_theme_background_c),
                        AppCtx.getIntegerRes(R.integer.pref_background_color_default));
    }
}
