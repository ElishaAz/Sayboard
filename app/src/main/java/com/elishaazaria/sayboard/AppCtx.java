package com.elishaazaria.sayboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.ArrayRes;
import androidx.annotation.BoolRes;
import androidx.annotation.IntegerRes;
import androidx.annotation.StringRes;

public class AppCtx {
    @SuppressLint("StaticFieldLeak") // App context
    private static Context appCtx;

    public static void setAppCtx(Context context) {
        appCtx = context.getApplicationContext();
    }

    public static Context getAppCtx() {
        return appCtx;
    }

    public static String getStringRes(@StringRes int res) {
        return appCtx.getString(res);
    }

    public static int getIntegerRes(@IntegerRes int res) {
        return appCtx.getResources().getInteger(res);
    }

    public static boolean getBoolRes(@BoolRes int res) {
        return appCtx.getResources().getBoolean(res);
    }

    public static String[] getStringArrayRes(@ArrayRes int res){
        return appCtx.getResources().getStringArray(res);
    }
}
