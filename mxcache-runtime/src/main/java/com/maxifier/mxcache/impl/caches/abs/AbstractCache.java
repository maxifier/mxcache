/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.abs;

import com.maxifier.mxcache.LightweightLock;
import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.interfaces.Statistics;
import com.maxifier.mxcache.storage.Storage;
import javax.annotation.Nullable;

import java.util.concurrent.locks.Lock;

/**
 * AbstractCache - parent for all caches.
 *
 * <p>This class extends LightweightLock making itself a lock. This is due to we want to save some memory.</p>
 *
 * <p>It holds a reference to statistics, and contains shorthand methods for
 * operating with statistics.</p>
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
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
