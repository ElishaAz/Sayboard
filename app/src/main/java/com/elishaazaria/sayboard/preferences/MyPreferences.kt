package com.elishaazaria.sayboard.preferences

import android.content.Context
import android.content.SharedPreferences
import com.elishaazaria.sayboard.AppCtx.appCtx
import com.elishaazaria.sayboard.AppCtx.getStringRes
import com.elishaazaria.sayboard.R

object MyPreferences {
    private var mySharedPref: SharedPreferences? = null

    val sharedPref: SharedPreferences
        get() {
            if (mySharedPref == null) {
                mySharedPref = appCtx!!.getSharedPreferences(
                    getStringRes(R.string.main_shared_pref), Context.MODE_PRIVATE
                )
            }
            return mySharedPref!!
        }
}