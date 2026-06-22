package com.eipna.centsation.data;

public enum DateFormat {
    EEEE_MMMM_DD_YYYY("EEEE, MMMM dd yyyy"),
    MM_DD_YYYY("MM/dd/yyyy"),
    DD_MM_YYYY("dd/MM/yyyy"),
    YYYY_DD_MM("yyyy/dd/MM"),
    YYYY_MM_DD("yyyy/MM/dd");

    public final String PATTERN;

    DateFormat(String pattern) {
        this.PATTERN = pattern;
    }
}