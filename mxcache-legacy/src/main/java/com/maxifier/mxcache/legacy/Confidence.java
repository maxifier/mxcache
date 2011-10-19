package com.maxifier.mxcache.legacy;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 01.07.2010
 * Time: 10:14:41
 */
public class Confidence {
    private final int callsToBeConfident;
    private final int periodsToBeConfident;

    public Confidence(int periodsToBeConfident, int callsToBeConfident) {
        this.periodsToBeConfident = periodsToBeConfident;
        this.callsToBeConfident = callsToBeConfident;
    }

    public int getCallsToBeConfident() {
        return callsToBeConfident;
    }

    public int getPeriodsToBeConfident() {
        return periodsToBeConfident;
    }

    @Override
    public String toString() {
        return "Confidence{callsToBeConfident = " + callsToBeConfident +
                 ", periodsToBeConfident = " + periodsToBeConfident + "}";
    }
}