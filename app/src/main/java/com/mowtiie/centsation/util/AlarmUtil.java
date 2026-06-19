package com.mowtiie.centsation.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.mowtiie.centsation.data.Database;
import com.mowtiie.centsation.data.saving.Saving;
import com.mowtiie.centsation.receiver.DeadlineReceiver;

public class AlarmUtil {

    public static final int NO_ALARM = 0;

    public static void set(Context context, Saving saving) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, DeadlineReceiver.class);
        intent.putExtra(Database.COLUMN_SAVING_ID, saving.getID());
        intent.putExtra(Database.COLUMN_SAVING_NAME, saving.getName());
        intent.putExtra(Database.COLUMN_SAVING_DEADLINE, saving.getDeadline());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                saving.getID().hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, saving.getDeadline(), pendingIntent);
    }

    public static void cancel(Context context, Saving saving) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, DeadlineReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                saving.getID().hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.cancel(pendingIntent);
    }
}