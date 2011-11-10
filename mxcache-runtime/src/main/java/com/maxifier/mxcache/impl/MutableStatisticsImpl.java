package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.interfaces.Statistics;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 29.08.11
 * Time: 18:24
 */
public class MutableStatisticsImpl implements MutableStatistics {
    private static final boolean USE_ATOMIC_UPDATERS = true;

    private static final AtomicIntegerFieldUpdater<MutableStatisticsImpl> HITS_UPDATER = USE_ATOMIC_UPDATERS ? AtomicIntegerFieldUpdater.newUpdater(MutableStatisticsImpl.class, "hits") : null;
    private static final AtomicIntegerFieldUpdater<MutableStatisticsImpl> MISSES_UPDATER = USE_ATOMIC_UPDATERS ? AtomicIntegerFieldUpdater.newUpdater(MutableStatisticsImpl.class, "misses") : null;
    private static final AtomicLongFieldUpdater<MutableStatisticsImpl> TIME_UPDATER = USE_ATOMIC_UPDATERS ? AtomicLongFieldUpdater.newUpdater(MutableStatisticsImpl.class, "time") : null;

    @SuppressWarnings({"UnusedDeclaration"})
    // these fields are updated through atomic updaters
    private volatile long time;
    private volatile int hits;
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
        if (USE_ATOMIC_UPDATERS) {
            // int assignment is guaranteed to be atomic
            hits = 0;
            misses = 0;
            TIME_UPDATER.set(this, 0L);
        } else {
            synchronized (this) {
                hits = 0;
                misses = 0;
                time = 0;
            }
        }
    }

    @Override
    public void hit() {
        if (USE_ATOMIC_UPDATERS) {
            HITS_UPDATER.incrementAndGet(this);
        } else {
            synchronized (this) {
                hits++;
            }
        }
    }

    @Override
    public synchronized void miss(long time) {
        if (USE_ATOMIC_UPDATERS) {
            MISSES_UPDATER.incrementAndGet(this);
            TIME_UPDATER.addAndGet(this, time);
        } else {
            synchronized (this) {
                misses++;
                this.time += time;
            }
        }
    }

    @Override
    public String toString() {
        return "Statistics{hits = " + hits + ", misses = " + misses + ", avg calculation = " + getAverageCalculationTime() + "}";
    }

    @Override
    @NotNull
    public Statistics getStatistics() {
        return this;
    }
}
