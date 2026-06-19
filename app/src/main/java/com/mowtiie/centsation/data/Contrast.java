package com.mowtiie.centsation.data;

public enum Contrast {
    LOW("contrast_low"),
    MEDIUM("contrast_medium"),
    HIGH("contrast_high");

    public final String VALUE;

    Contrast(String value) {
        this.VALUE = value;
    }
}