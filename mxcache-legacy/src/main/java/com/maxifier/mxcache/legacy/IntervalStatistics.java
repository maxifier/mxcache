package com.maxifier.mxcache.legacy;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 08.11.2010
* Time: 16:30:15
*/
class IntervalStatistics {
    private int n;
    private double sum;
    private double max;
    private double min;

    public void add(long value) {
        if (n == 0) {
            sum = value;
            max = value;
            min = value;
            n = 1;
        } else {
            n++;
            sum += value;
            max = Math.max(max, value);
            min = Math.min(min, value);
        }
    }

    public double getAverage() {
        return sum / n;
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }
}
