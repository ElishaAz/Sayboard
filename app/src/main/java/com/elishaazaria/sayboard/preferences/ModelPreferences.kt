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
import java.util.*
import kotlin.collections.HashSet

object ModelPreferences {
    const val VOSK_SERVER_ENABLED = false
    @JvmStatic
    var voskServers: List<VoskServerData>
        get() {
            val set = MyPreferences.sharedPref.getStringSet(
                getStringRes(R.string.pref_models_vosk_servers_set),
                null
            ) ?: return ArrayList()
            val list = ArrayList<VoskServerData>()
            for (stringData in set) {
                val data = VoskServerData.deserialize(stringData)
                if (data != null) {
                    list.add(data)
                }
            }
            Collections.sort(list)
            return list
        }
        set(servers) {
            val set: MutableSet<String> = HashSet()
            for (data in servers) {
                set.add(VoskServerData.serialize(data))
            }
            MyPreferences.sharedPref.edit()
                .putStringSet(getStringRes(R.string.pref_models_vosk_servers_set), set).apply()
        }

    fun addToVoskServers(data: VoskServerData) {
        val set: MutableSet<String> = HashSet(
            MyPreferences.sharedPref.getStringSet(
                getStringRes(R.string.pref_models_vosk_servers_set),
                HashSet()
            )
        )
        set.add(VoskServerData.serialize(data))
        MyPreferences.sharedPref.edit()
            .putStringSet(getStringRes(R.string.pref_models_vosk_servers_set), set).apply()
    }

    fun removeFromVoskServers(data: VoskServerData) {
        val set: MutableSet<String> = HashSet(
            MyPreferences.sharedPref.getStringSet(
                getStringRes(R.string.pref_models_vosk_servers_set),
                HashSet()
            )
        )
        set.remove(VoskServerData.serialize(data))
        MyPreferences.sharedPref.edit()
            .putStringSet(getStringRes(R.string.pref_models_vosk_servers_set), set).apply()
    }
}