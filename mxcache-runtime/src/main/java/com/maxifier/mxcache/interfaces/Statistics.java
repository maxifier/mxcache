package com.maxifier.mxcache.interfaces;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 16.08.2010
 * Time: 14:10:15
 *
 * <p>
 * Node: statistics may reflect current state of cache so it may be changed in another thread or event be reset.
 */
public interface Statistics {
    /**
     * @return number of cache-hits since last reset
     */
    int getHits();

    /**
     * @return number of cache-misses since last reset
     */
    int getMisses();

    long getTotalCalculationTime();

    /**
     * @return average calculation time in ns
     */
    double getAverageCalculationTime();

    /**
     * Resets statistics. Reset may be unsupported by implementation (e.g. if this statistics reflects past state).
     * No exception will be thrown than.
     */
    void reset();
}
