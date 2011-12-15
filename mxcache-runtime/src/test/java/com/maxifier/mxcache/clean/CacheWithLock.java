package com.maxifier.mxcache.clean;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.impl.resource.DependencyTracker;
import com.maxifier.mxcache.interfaces.Statistics;
import com.maxifier.mxcache.provider.CacheDescriptor;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 27.04.2010
 * Time: 17:24:56
 */
class CacheWithLock implements Cache {
    private final ReentrantLock lock;

    public CacheWithLock(ReentrantLock lock) {
        this.lock = lock;
    }

    @Override
    public Lock getLock() {
        return lock;
    }

    @Override
    public void clear() {
        assert lock.isHeldByCurrentThread();
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public Statistics getStatistics() {
        return null;
    }

    @Override
    public CacheDescriptor getDescriptor() {
        return null;
    }

    @Override
    public DependencyNode getDependencyNode() {
        return DependencyTracker.DUMMY_NODE;
    }
}
