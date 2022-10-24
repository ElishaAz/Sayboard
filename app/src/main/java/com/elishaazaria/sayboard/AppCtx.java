package com.elishaazaria.sayboard;

import android.annotation.SuppressLint;
import android.content.Context;

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

    public static int getIntegerRes(@IntegerRes int res){
        return appCtx.getResources().getInteger(res);
    }
}
