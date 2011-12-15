package com.maxifier.mxcache.impl.caches.abs.elementlocked;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.interfaces.Statistics;

import java.util.concurrent.locks.Lock;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 08.09.2010
 * Time: 9:51:52
 */
abstract class AbstractElementLockedCache implements Cache, ElementLockedStorage {
    private final MutableStatistics statistics;

    protected AbstractElementLockedCache(MutableStatistics statistics) {
        this.statistics = statistics;
    }

    public void miss(long dt) {
        statistics.miss(dt);
    }

    public void hit() {
        statistics.hit();
    }

    @Override
    public Statistics getStatistics() {
        return statistics;
    }

    @Override
    public int getSize() {
        Lock lock = getLock();
        if (lock == null) {
            return size();
        }
        lock.lock();
        try {
            return size();
        } finally {
            lock.unlock();
        }
    }
}
