/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.caches;

import com.maxifier.mxcache.impl.resource.DependencyNode;
import javax.annotation.Nullable;

import java.util.concurrent.locks.Lock;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface CleaningNode {
    /**
     * @return lock if any
     */
    @Nullable
    Lock getLock();

    /**
     * Clears the cache.
     * This method requires a lock, but it doesn't lock anything.
     * Calling it without proper locking may result in undefined behaviour
     * (e.g. ConcurrentModificationException or deadlocks)
     *
     * Lock must be obtained first with {@link #getLock()} and locked.
     */
    void clear();

    DependencyNode getDependencyNode();

    /**
     * @return owner of cache, null for static caches.
     */
    @Nullable
    Object getCacheOwner();
}
