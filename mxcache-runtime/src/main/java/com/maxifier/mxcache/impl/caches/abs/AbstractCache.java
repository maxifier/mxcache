package com.maxifier.mxcache.impl.caches.abs;

import com.maxifier.mxcache.LightweightLock;
import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.interfaces.Statistics;
import com.maxifier.mxcache.storage.Storage;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.Lock;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 08.09.2010
 * Time: 9:51:52
 */
abstract class AbstractCache extends LightweightLock implements Cache, Storage {
    private final MutableStatistics statistics;

    private DependencyNode node;
    protected final Object owner;

    protected AbstractCache(Object owner, @Nullable MutableStatistics statistics) {
        this.owner = owner;
        this.statistics = statistics;
    }

    @Override
    public void setDependencyNode(DependencyNode node) {
        this.node = node;
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

    @Override
    public int getSize() {
        lock();
        try {
            return size();
        } finally {
            unlock();
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
