package com.eipna.centsation.ui.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.eipna.centsation.R;
import com.eipna.centsation.data.Contrast;
import com.eipna.centsation.data.Theme;
import com.eipna.centsation.util.NotificationHandler;
import com.eipna.centsation.util.PreferenceUtil;
import com.google.android.material.color.DynamicColors;

public abstract class CentsationActivity extends AppCompatActivity {

    protected PreferenceUtil preferenceUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        preferenceUtil = new PreferenceUtil(this);
        super.onCreate(savedInstanceState);

        if (preferenceUtil.isScreenPrivacyEnabled()) {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
            );
        }

        NotificationHandler.createChannels(this);

        String theme = preferenceUtil.getTheme();
        if (theme.equals(Theme.SYSTEM.VALUE)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else if (theme.equals(Theme.BATTERY.VALUE)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
        } else if (theme.equals(Theme.LIGHT.VALUE)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (theme.equals(Theme.DARK.VALUE)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        String contrast = preferenceUtil.getContrast();
        if (contrast.equals(Contrast.LOW.VALUE)) {
            setTheme(R.style.Theme_Centsation);
        } else if (contrast.equals(Contrast.MEDIUM.VALUE)) {
            setTheme(R.style.Theme_Centsation_MediumContrast);
        } else if (contrast.equals(Contrast.HIGH.VALUE)) {
            setTheme(R.style.Theme_Centsation_HighContrast);
        }

        if (preferenceUtil.isDynamicColors()) DynamicColors.applyToActivityIfAvailable(this);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        recreate();
    }
}