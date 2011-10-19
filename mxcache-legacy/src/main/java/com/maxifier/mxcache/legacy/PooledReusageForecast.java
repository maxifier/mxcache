package com.maxifier.mxcache.legacy;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 10.11.2010
 * Time: 10:15:52
 */
public final class PooledReusageForecast {
    private static final int FACTOR_BOOST = 1;
    private static final int PERIODS_BOOST = 5;
    private static final float MIN_FACTOR = 1e-2f;
    private static final float MAX_FACTOR = 1.0f;
    private static final int SCALER = 5;

    private PooledReusageForecast() {
    }

    public static float getCallForecast(Confidence confidence, int calls, int periods, int currentPeriod, int lastUsePeriod) {
        float callsFactor = clamp((float) (calls + FACTOR_BOOST) / confidence.getCallsToBeConfident());
        float periodsFactor = clamp((float) (periods + FACTOR_BOOST) / confidence.getPeriodsToBeConfident());
        return (calls * callsFactor * periodsFactor) * SCALER / (currentPeriod - lastUsePeriod + PERIODS_BOOST);
    }

    private static float clamp(float callsFactor) {
        if (callsFactor < MIN_FACTOR) {
            return MIN_FACTOR;
        }
        if (callsFactor > MAX_FACTOR) {
            return MAX_FACTOR;
        }
        return callsFactor;
    }
}
