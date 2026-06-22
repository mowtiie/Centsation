package com.eipna.centsation;

import android.app.Application;

import com.eipna.centsation.util.CrashHandler;

public class CentsationApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.install(this);
    }
}
