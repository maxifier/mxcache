package com.maxifier.mxcache.jconsoleplugin;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 18.10.2010
* Time: 9:07:43
*/
final class Time implements Comparable<Time> {
    private final double value;

    Time(double value) {
        this.value = value;
    }

    @Override
    public int compareTo(Time o) {
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

        Time time = (Time) o;

        return compareTo(time) == 0;
    }

    @Override
    public int hashCode() {
        long temp = value != +0.0d ? Double.doubleToLongBits(value) : 0L;
        return (int) (temp ^ (temp >>> 32));
    }

    @Override
    public String toString() {
        if (value < 1000.0) {
            return (int)value + "ns";
        }
        if (value < 1000000.0) {
            return (int)(value/1000) + "us";
        }
        return (int)(value/1000000) + "ms";
    }
}
