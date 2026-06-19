package com.mowtiie.centsation.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mowtiie.centsation.data.saving.Saving;
import com.mowtiie.centsation.data.saving.SavingRepository;
import com.mowtiie.centsation.util.AlarmUtil;
import com.mowtiie.centsation.util.NotificationUtil;

import java.util.ArrayList;

public class DeadlineReceiver extends BroadcastReceiver {

    private static final String TAG = "DeadlineReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            rescheduleDeadlines(context);
        } else {
            NotificationUtil.create(context, intent);
        }
    }

    private void rescheduleDeadlines(Context context) {
        try (SavingRepository savingRepository = new SavingRepository(context)) {
            ArrayList<Saving> savings = new ArrayList<>(savingRepository.getSavings(Saving.NOT_ARCHIVE));
            long now = System.currentTimeMillis();
            for (Saving saving : savings) {
                if (saving.getDeadline() != AlarmUtil.NO_ALARM
                        && saving.getDeadline() > now) {
                    AlarmUtil.set(context, saving);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while rescheduling deadlines", e);
        }
    }
}