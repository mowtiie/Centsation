package com.eipna.centsation.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.eipna.centsation.data.saving.Saving;
import com.eipna.centsation.data.saving.SavingRepository;
import com.eipna.centsation.util.AlarmSetter;
import com.eipna.centsation.util.NotificationHandler;

import java.util.ArrayList;

public class DeadlineReceiver extends BroadcastReceiver {

    private static final String TAG = "DeadlineReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            rescheduleDeadlines(context);
        } else {
            NotificationHandler.create(context, intent);
        }
    }

    private void rescheduleDeadlines(Context context) {
        try (SavingRepository savingRepository = new SavingRepository(context)) {
            ArrayList<Saving> savings = new ArrayList<>(savingRepository.getSavings(Saving.NOT_ARCHIVE));
            long now = System.currentTimeMillis();
            for (Saving saving : savings) {
                if (saving.getDeadline() != AlarmSetter.NO_ALARM
                        && saving.getDeadline() > now) {
                    AlarmSetter.set(context, saving);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while rescheduling deadlines", e);
        }
    }
}