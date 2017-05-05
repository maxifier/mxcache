/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.jconsoleplugin;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
final class Rate implements Comparable<Rate> {
    private final double value;

    Rate(double value) {
        this.value = value;
    }

    @Override
    public int compareTo(Rate o) {
        return Double.compare(value, o.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Rate rate = (Rate) o;

        return compareTo(rate) == 0;

    }

    @Override
    public int hashCode() {
        long temp = value != +0.0d ? Double.doubleToLongBits(value) : 0L;
        return (int) (temp ^ (temp >>> 32));
    }

    @Override
    public String toString() {
        double percent = value * 100.0;
        if (Double.isNaN(percent)) {
            return "-";
        }
        if (percent == 0.0) {
            return "0";
        }
        if (percent < 1) {
            return "<1";
        }
        if (percent == 100.0) {
            return "100";
        }
        if (percent > 99.0) {
            return ">99";
        }
        return Long.toString(Math.round(percent));
    }
}
