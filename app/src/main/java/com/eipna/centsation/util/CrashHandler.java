package com.eipna.centsation.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private final Context context;
    private final Thread.UncaughtExceptionHandler defaultHandler;

    private CrashHandler(Context context) {
        this.context = context.getApplicationContext();
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    public static void install(Context context) {
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(context));
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
        try {
            writeReport(thread, throwable);
        } catch (Throwable ignored) {
        }

        if (defaultHandler != null) {
            defaultHandler.uncaughtException(thread, throwable);
        } else {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }

    private void writeReport(Thread thread, Throwable throwable) throws Exception {
        File reportFile = CrashReporter.getReportFile(context);
        File parent = reportFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(reportFile), StandardCharsets.UTF_8)) {
            writer.write(buildReport(thread, throwable));
        }
    }

    private String buildReport(Thread thread, Throwable throwable) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== Centsation Crash Report ===\n");
        sb.append("Time: ").append(new Date()).append("\n");
        sb.append("Thread: ").append(thread.getName()).append("\n");
        sb.append("\n");

        sb.append("--- App Info ---\n");
        sb.append("Package: ").append(context.getPackageName()).append("\n");
        try {
            PackageInfo pkg = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            sb.append("Version: ").append(pkg.versionName).append(" (").append(pkg.versionCode).append(")\n");
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        sb.append("\n");

        sb.append("--- Device Info ---\n");
        sb.append("Manufacturer: ").append(Build.MANUFACTURER).append("\n");
        sb.append("Model: ").append(Build.MODEL).append("\n");
        sb.append("Android: ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n");
        sb.append("\n");

        sb.append("--- Stack Trace ---\n");
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        sb.append(sw);

        return sb.toString();
    }
}