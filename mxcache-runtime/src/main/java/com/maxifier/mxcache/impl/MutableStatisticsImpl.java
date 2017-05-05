/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.interfaces.Statistics;

import javax.annotation.Nonnull;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class MutableStatisticsImpl implements MutableStatistics {
    private static final AtomicIntegerFieldUpdater<MutableStatisticsImpl> HITS_UPDATER = AtomicIntegerFieldUpdater.newUpdater(MutableStatisticsImpl.class, "hits");
    private static final AtomicIntegerFieldUpdater<MutableStatisticsImpl> MISSES_UPDATER = AtomicIntegerFieldUpdater.newUpdater(MutableStatisticsImpl.class, "misses");
    private static final AtomicLongFieldUpdater<MutableStatisticsImpl> TIME_UPDATER = AtomicLongFieldUpdater.newUpdater(MutableStatisticsImpl.class, "time");

    @SuppressWarnings({"UnusedDeclaration"})
    // these fields are updated through atomic updaters
    private volatile long time;
    @SuppressWarnings({"UnusedDeclaration"})
    // these fields are updated through atomic updaters
    private volatile int hits;
    @SuppressWarnings({"UnusedDeclaration"})
    // these fields are updated through atomic updaters
    private volatile int misses;

    @Override
    public int getHits() {
        return hits;
    }

    @Override
    public int getMisses() {
        return misses;
    }

    @Override
    public double getAverageCalculationTime() {
        return ((double)time)/misses;
    }

    @Override
    public long getTotalCalculationTime() {
        return time;
    }

    @Override
    public void reset() {
        HITS_UPDATER.set(this, 0);
        MISSES_UPDATER.set(this, 0);
        TIME_UPDATER.set(this, 0L);
    }

    @Override
    public void hit() {
        HITS_UPDATER.incrementAndGet(this);
    }

    @Override
    public synchronized void miss(long time) {
        MISSES_UPDATER.incrementAndGet(this);
        TIME_UPDATER.addAndGet(this, time);
    }

    @Override
    public String toString() {
        return "Statistics{hits = " + hits + ", misses = " + misses + ", avg calculation = " + getAverageCalculationTime() + "}";
    }

    @Override
    @Nonnull
    public Statistics getStatistics() {
        return this;
    }
}