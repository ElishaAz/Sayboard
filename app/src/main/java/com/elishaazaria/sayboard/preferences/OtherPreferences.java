package com.elishaazaria.sayboard.preferences;

import android.util.Log;

import com.elishaazaria.sayboard.AppCtx;
import com.elishaazaria.sayboard.R;

public class OtherPreferences {
    public static final int KEEP_SCREEN_AWAKE_NEVER = 0;
    public static final int KEEP_SCREEN_AWAKE_WHEN_LISTENING = 1;
    public static final int KEEP_SCREEN_AWAKE_WHEN_OPEN = 2;

    public static int getKeepScreenAwake() {
        String val = MyPreferences.getSharedPref()
                .getString(AppCtx.getStringRes(R.string.pref_other_keep_screen_awake_l),
                        AppCtx.getStringRes(R.string.pref_keep_awake_default));

        if (val.equals(AppCtx.getStringRes(R.string.value_keep_awake_never)))
            return KEEP_SCREEN_AWAKE_NEVER;
        if (val.equals(AppCtx.getStringRes(R.string.value_keep_awake_when_listening)))
            return KEEP_SCREEN_AWAKE_WHEN_LISTENING;
        if (val.equals(AppCtx.getStringRes(R.string.value_keep_awake_when_open)))
            return KEEP_SCREEN_AWAKE_WHEN_OPEN;

        return -1;
    }

    public static float getScreenHeightPortrait() {
        return MyPreferences.getSharedPref().getInt(AppCtx.getStringRes(R.string.pref_other_keyboard_height_portrait_i),
                AppCtx.getIntegerRes(R.integer.pref_keyboard_height_portrait_default)) / (float) AppCtx.getIntegerRes(R.integer.keyboard_height_max);
    }

    public static float getScreenHeightLandscape() {
        return MyPreferences.getSharedPref().getInt(AppCtx.getStringRes(R.string.pref_other_keyboard_height_landscape_i),
                AppCtx.getIntegerRes(R.integer.pref_keyboard_height_landscape_default)) / (float) AppCtx.getIntegerRes(R.integer.keyboard_height_max);
    }
}
