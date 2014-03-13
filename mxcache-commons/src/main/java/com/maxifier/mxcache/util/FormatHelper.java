/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.util;

import java.math.BigDecimal;

/**
 * FormatHelper
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class FormatHelper {
    private static final int KILOBYTE = 1024;
    private static final int MEGABYTE = 1024 * KILOBYTE;
    private static final String MEGABYTE_POSTFIX = "m";
    private static final String MEGABYTE_POSTFIX_CAPITAL = MEGABYTE_POSTFIX.toUpperCase();
    private static final String KILOBYTE_POSTFIX = "k";
    private static final String KILOBYTE_POSTFIX_CAPITAL = KILOBYTE_POSTFIX.toUpperCase();

    private FormatHelper() {
    }

    public static String formatSize(long progress) {
        if (progress >= MEGABYTE) {
            return format(progress, MEGABYTE_POSTFIX, MEGABYTE);
        }
        if ((progress >= KILOBYTE)) {
            return format(progress, KILOBYTE_POSTFIX, KILOBYTE);
        }
        return Long.toString(progress);
    }

    private static String format(long progress, String postfix, int scaler) {
        double value = (double) progress / scaler;
        String s = round(value, 1).toString();
        return zeroCutting(s) + " " + postfix;
    }

    private static BigDecimal round(Number figure, int signCount) {
        return BigDecimal.valueOf(figure.doubleValue()).setScale(signCount, BigDecimal.ROUND_HALF_UP);
    }

    private static String zeroCutting(String s) {
        if (s.endsWith(".0")) {
            return s.substring(0, s.length() - 2);
        }
        return s;
    }

    public static double parseSize(String s) {
        return parseSuffixedNumber0(s.trim());
    }

    private static double parseSuffixedNumber0(String s) {
        if (s.endsWith(MEGABYTE_POSTFIX) || s.endsWith(MEGABYTE_POSTFIX_CAPITAL)) {
            return MEGABYTE * Double.parseDouble(s.substring(0, s.length() - 1).trim());
        }
        if (s.endsWith(KILOBYTE_POSTFIX) || s.endsWith(KILOBYTE_POSTFIX_CAPITAL)) {
            return KILOBYTE * Double.parseDouble(s.substring(0, s.length() - 1).trim());
        }
        return Double.parseDouble(s);
    }
}
