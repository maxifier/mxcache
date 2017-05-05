/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.interfaces;

/**
 * Statistics
 *
 * Node: statistics may reflect current state of cache so it may be changed in another thread or event be reset.
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
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

    /**
     * @return the total calculation time since last reset in ns
     */
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
