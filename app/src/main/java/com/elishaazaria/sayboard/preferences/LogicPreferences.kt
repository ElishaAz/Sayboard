package com.elishaazaria.sayboard.preferences

import com.elishaazaria.sayboard.AppCtx.getStringRes
import com.elishaazaria.sayboard.AppCtx.getBoolRes
import com.elishaazaria.sayboard.AppCtx.appCtx
import com.elishaazaria.sayboard.AppCtx.getIntegerRes
import com.elishaazaria.sayboard.preferences.MyPreferences
import com.elishaazaria.sayboard.AppCtx
import com.elishaazaria.sayboard.R
import com.elishaazaria.sayboard.preferences.LogicPreferences
import com.elishaazaria.sayboard.data.VoskServerData
import android.content.SharedPreferences
import android.os.Build
import com.elishaazaria.sayboard.preferences.UIPreferences
import androidx.core.content.ContextCompat

object LogicPreferences {
    const val KEEP_SCREEN_AWAKE_NEVER = 0
    const val KEEP_SCREEN_AWAKE_WHEN_LISTENING = 1
    const val KEEP_SCREEN_AWAKE_WHEN_OPEN = 2
    @JvmStatic
    val keepScreenAwake: Int
        get() {
            val `val` = MyPreferences.sharedPref
                .getString(
                    getStringRes(R.string.pref_logic_keep_screen_awake_l),
                    getStringRes(R.string.pref_keep_awake_default)
                )
            if (`val` == getStringRes(R.string.value_keep_awake_never)) return KEEP_SCREEN_AWAKE_NEVER
            if (`val` == getStringRes(R.string.value_keep_awake_when_listening)) return KEEP_SCREEN_AWAKE_WHEN_LISTENING
            return if (`val` == getStringRes(R.string.value_keep_awake_when_open)) KEEP_SCREEN_AWAKE_WHEN_OPEN else -1
        }
    val isListenImmediately: Boolean
        get() = MyPreferences.sharedPref.getBoolean(
            getStringRes(R.string.pref_logic_listen_immediately_b),
            getBoolRes(R.bool.pref_listen_immediately_default)
        )
    val isAutoSwitchBack: Boolean
        get() = MyPreferences.sharedPref.getBoolean(
            getStringRes(R.string.pref_logic_auto_switch_back_b),
            getBoolRes(R.bool.pref_auto_switch_back_default)
        )
    val isWeakRefModel: Boolean
        get() = MyPreferences.sharedPref.getBoolean(
            getStringRes(R.string.pref_logic_weak_ref_model_b),
            getBoolRes(R.bool.pref_weak_ref_model_default)
        )
}