package com.elishaazaria.sayboard

import android.app.Application
import com.elishaazaria.sayboard.AppCtx.setAppCtx
import dev.patrickgold.jetpref.datastore.JetPref
import org.greenrobot.eventbus.EventBus

class SayboardApplication : Application() {
    private val prefs by sayboardPreferenceModel()
    override fun onCreate() {
        super.onCreate()

        // Optionally initialize global JetPref configs. This must be done before
        // any preference datastore is initialized!
        JetPref.configure(
            saveIntervalMs = 500,
            encodeDefaultValues = true,
        )

        // Initialize your datastore here (required)
        prefs.initializeBlocking(this)

        EventBus.builder().logNoSubscriberMessages(false).sendNoSubscriberEvent(false)
            .installDefaultEventBus()
        setAppCtx(this)
    }
}