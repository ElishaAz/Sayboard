package com.elishaazaria.sayboard

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.ArrayRes
import androidx.annotation.BoolRes
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes

@SuppressLint("StaticFieldLeak")
object AppCtx {
    @JvmStatic
    @SuppressLint("StaticFieldLeak") // App context
    var appCtx: Context? = null
        private set

    @JvmStatic
    fun setAppCtx(context: Context) {
        appCtx = context.applicationContext
    }

    @JvmStatic
    fun getStringRes(@StringRes res: Int): String {
        return appCtx!!.getString(res)
    }

    @JvmStatic
    fun getIntegerRes(@IntegerRes res: Int): Int {
        return appCtx!!.resources.getInteger(res)
    }

    @JvmStatic
    fun getBoolRes(@BoolRes res: Int): Boolean {
        return appCtx!!.resources.getBoolean(res)
    }

    fun getStringArrayRes(@ArrayRes res: Int): Array<String> {
        return appCtx!!.resources.getStringArray(res)
    }
}