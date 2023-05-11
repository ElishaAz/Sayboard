package com.elishaazaria.sayboard

import android.app.Application
import com.elishaazaria.sayboard.AppCtx.setAppCtx
import org.greenrobot.eventbus.EventBus

class SayboardApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        EventBus.builder().logNoSubscriberMessages(false).sendNoSubscriberEvent(false)
            .installDefaultEventBus()
        setAppCtx(this)
    }
}