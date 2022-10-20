package com.elishaazaria.sayboard;

import android.app.Application;

import org.greenrobot.eventbus.EventBus;

public class SayboardApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.builder().logNoSubscriberMessages(false).sendNoSubscriberEvent(false).installDefaultEventBus();
    }
}
