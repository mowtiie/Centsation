package com.mowtiie.centsation.data;

public enum Theme {
    LIGHT("Light"),
    DARK("Dark"),
    BATTERY("Battery Saving"),
    SYSTEM("System Default");

    public final String VALUE;

    Theme(String value) {
        this.VALUE = value;
    }
}