package com.mowtiie.centsation;

import android.app.Application;

import com.mowtiie.centsation.util.CrashHandler;

public class CentsationApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.install(this);
    }
}
