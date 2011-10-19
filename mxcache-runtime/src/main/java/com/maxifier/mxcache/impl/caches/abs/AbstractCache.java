package com.maxifier.mxcache.impl.caches.abs;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.interfaces.Statistics;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 08.09.2010
 * Time: 9:51:52
 */
abstract class AbstractCache extends ReentrantLock implements Cache, Lock {
    private final MutableStatistics statistics;

    protected AbstractCache(@Nullable MutableStatistics statistics) {
        this.statistics = statistics;
    }

    @Override
    public Lock getLock() {
        return this;
    }

    @Override
    public Statistics getStatistics() {
        return statistics;
    }

    protected final void miss(long dt) {
        if (statistics != null) {
            statistics.miss(dt);
        }
    }

    protected final void hit() {
        if (statistics != null) {
            statistics.hit();
        }
    }
}
