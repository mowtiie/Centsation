package com.eipna.centsation.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.eipna.centsation.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CrashReporter {

    private static final String REPORT_FILENAME = "last_crash.txt";

    public static File getReportFile(Context context) {
        return new File(context.getFilesDir(), REPORT_FILENAME);
    }

    public static boolean hasReport(Context context) {
        return getReportFile(context).exists();
    }

    @Nullable
    public static String readReport(Context context) {
        File file = getReportFile(context);
        if (!file.exists()) return null;

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }

    public static void deleteReport(Context context) {
        File file = getReportFile(context);
        if (file.exists()) {
            file.delete();
        }
    }

    public static void showDialogIfPending(Activity activity, ActivityResultLauncher<Intent> saveLauncher) {
        if (!hasReport(activity)) return;

        View crashDialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_crash, null, false);

        MaterialButton actionEmail = crashDialogView.findViewById(R.id.crash_action_email);
        MaterialButton actionSave = crashDialogView.findViewById(R.id.crash_action_save);
        MaterialButton actionDismiss = crashDialogView.findViewById(R.id.crash_action_dismiss);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.dialog_title_crash)
                .setMessage(R.string.dialog_message_crash)
                .setIcon(R.drawable.ic_crash_error)
                .setCancelable(false)
                .setView(crashDialogView);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            actionEmail.setOnClickListener(view -> {
                String report = readReport(activity);
                if (report == null) return;
                if (sendEmail(activity, report)) {
                    deleteReport(activity);
                }
                dialog.dismiss();
            });

            actionSave.setOnClickListener(view -> {
                launchSaveDialog(saveLauncher);
                dialog.dismiss();
            });

            actionDismiss.setOnClickListener(view -> {
                deleteReport(activity);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    public static boolean writeReportToUri(Context context, Uri uri) {
        String report = readReport(context);
        if (report == null) return false;

        try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
            if (out == null) return false;
            out.write(report.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static String suggestedFilename() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        return "centsation_crash_" + sdf.format(new Date()) + ".txt";
    }

    private static boolean sendEmail(Activity activity, String report) {
        String developerEmail = activity.getString(R.string.developer_email);

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{developerEmail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.crash_email_subject));
        emailIntent.putExtra(Intent.EXTRA_TEXT, report);

        try {
            activity.startActivity(Intent.createChooser(emailIntent, activity.getString(R.string.crash_email_chooser_title)));
            return true;
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.crash_no_email_app_toast, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private static void launchSaveDialog(ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, suggestedFilename());
        launcher.launch(intent);
    }
}