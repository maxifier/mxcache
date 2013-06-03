package com.maxifier.mxcache.impl.caches.abs.elementlocked;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.interfaces.Statistics;

import java.util.concurrent.locks.Lock;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 08.09.2010
 * Time: 9:51:52
 */
abstract class AbstractElementLockedCache implements Cache, ElementLockedStorage {
    protected final Object owner;
    private final MutableStatistics statistics;

    private DependencyNode node;

    protected AbstractElementLockedCache(Object owner, MutableStatistics statistics) {
        this.owner = owner;
        this.statistics = statistics;
    }

    @Override
    public void setDependencyNode(DependencyNode node) {
        this.node = node;
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

    @Override
    public DependencyNode getDependencyNode() {
        return node;
    }


    @Override
    public Object getCacheOwner() {
        return owner;
    }

}
