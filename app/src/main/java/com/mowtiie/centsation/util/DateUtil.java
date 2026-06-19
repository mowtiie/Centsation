package com.mowtiie.centsation.util;

import android.icu.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;

public class DateUtil {
    public static String getStringDate(long timeStamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault());
        return dateFormat.format(new Date(timeStamp));
    }

    public static String getStringDate(long timeStamp, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        return dateFormat.format(new Date(timeStamp));
    }
}