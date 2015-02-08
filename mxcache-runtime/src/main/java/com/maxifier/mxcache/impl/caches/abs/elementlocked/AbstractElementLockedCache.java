/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.abs.elementlocked;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.interfaces.Statistics;

import java.util.concurrent.locks.Lock;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
abstract class AbstractElementLockedCache implements Cache, ElementLockedStorage {
    protected final Object owner;
    private final MutableStatistics statistics;

    private DependencyNode node;
    private boolean dirty;

    protected AbstractElementLockedCache(Object owner, MutableStatistics statistics) {
        this.owner = owner;
        this.statistics = statistics;
    }

    @Override
    public void setDependencyNode(DependencyNode node) {
        this.node = node;
    }

    protected void miss(long dt) {
        statistics.miss(dt);
    }

    protected void hit() {
        statistics.hit();
    }

    @Override
    public Statistics getStatistics() {
        return statistics;
    }

    @Override
    public void invalidate() {
        Lock lock = getLock();
        dirty = true;
        if (lock.tryLock()) {
            try {
                if (dirty) {
                    clear();
                    dirty = false;
                }
            } finally {
                lock.unlock();
            }
        }
    }

    protected void postCheckDirty() {
        if (dirty) {
            Lock lock = getLock();
            if (lock.tryLock()) {
                try {
                    if (dirty) {
                        clear();
                        dirty = false;
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    protected void preCheckDirty() {
        if (dirty) {
            Lock lock = getLock();
            lock.lock();
            try {
                if (dirty) {
                    clear();
                    dirty = false;
                }
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public int getSize() {
        Lock lock = getLock();
        lock.lock();
        try {
            return size();
        } finally {
            lock.unlock();
        }
    }

    protected DependencyNode getDependencyNode() {
        return node;
    }
}
