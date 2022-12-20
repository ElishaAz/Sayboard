package com.elishaazaria.sayboard.preferences;

import com.elishaazaria.sayboard.AppCtx;
import com.elishaazaria.sayboard.R;

public class LogicPreferences {
    public static final int KEEP_SCREEN_AWAKE_NEVER = 0;
    public static final int KEEP_SCREEN_AWAKE_WHEN_LISTENING = 1;
    public static final int KEEP_SCREEN_AWAKE_WHEN_OPEN = 2;

    public static int getKeepScreenAwake() {
        String val = MyPreferences.getSharedPref()
                .getString(AppCtx.getStringRes(R.string.pref_logic_keep_screen_awake_l),
                        AppCtx.getStringRes(R.string.pref_keep_awake_default));

        if (val.equals(AppCtx.getStringRes(R.string.value_keep_awake_never)))
            return KEEP_SCREEN_AWAKE_NEVER;
        if (val.equals(AppCtx.getStringRes(R.string.value_keep_awake_when_listening)))
            return KEEP_SCREEN_AWAKE_WHEN_LISTENING;
        if (val.equals(AppCtx.getStringRes(R.string.value_keep_awake_when_open)))
            return KEEP_SCREEN_AWAKE_WHEN_OPEN;

        return -1;
    }

    public static boolean isListenImmediately() {
        return MyPreferences.getSharedPref().getBoolean(AppCtx.getStringRes(R.string.pref_logic_listen_immediately_b),
                AppCtx.getBoolRes(R.bool.pref_listen_immediately_default));
    }

    public static boolean isAutoSwitchBack() {
        return MyPreferences.getSharedPref().getBoolean(AppCtx.getStringRes(R.string.pref_logic_auto_switch_back_b),
                AppCtx.getBoolRes(R.bool.pref_auto_switch_back_default));
    }

    public static boolean isWeakRefModel() {
        return MyPreferences.getSharedPref().getBoolean(AppCtx.getStringRes(R.string.pref_logic_weak_ref_model_b),
                AppCtx.getBoolRes(R.bool.pref_weak_ref_model_default));
    }
}
