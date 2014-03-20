/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.abs.elementlocked;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.CacheId;
import com.maxifier.mxcache.impl.CalculatableHelper;
import com.maxifier.mxcache.impl.resource.*;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.storage.elementlocked.*;

import java.util.concurrent.locks.Lock;


/**
 * AbstractLongCache
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PCache.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public abstract class AbstractLongCache extends AbstractElementLockedCache implements LongCache, LongElementLockedStorage {
    private final LongCalculatable calculatable;

    public AbstractLongCache(Object owner, LongCalculatable calculatable, MutableStatistics statistics) {
        super(owner, statistics);
        this.calculatable = calculatable;
    }

    @Override
    public long getOrCreate() {
        if (DependencyTracker.isBypassCaches()) {
            return calculatable.calculate(owner);
        } else {
            Lock lock = getLock();
            if (lock != null) {
                lock.lock();
            }
            try {
                if (isCalculated()) {
                    DependencyTracker.mark(getDependencyNode());
                    hit();
                    return load();
                }

                DependencyNode callerNode = DependencyTracker.track(getDependencyNode());
                try {
                    while(true) {
                        try {
                            return create();
                        } catch (ResourceOccupied e) {
                            if (callerNode != null) {
                                throw e;
                            } else {
                                if (lock != null) {
                                    lock.unlock();
                                }
                                try {
                                    e.getResource().waitForEndOfModification();
                                } finally {
                                    if (lock != null) {
                                        lock.lock();
                                    }
                                }
                                if (isCalculated()) {
                                    hit();
                                    return load();
                                }
                            }
                        }
                    }
                } finally {
                    DependencyTracker.exit(callerNode);
                }
            } finally {
                if (lock != null) {
                    lock.unlock();
                }
            }
        }
    }

    protected long create() {
        long start = System.nanoTime();
        long t = calculatable.calculate(owner);
        long end = System.nanoTime();
        miss(end - start);
        save(t);
        return t;
    }

    @Override
    public CacheDescriptor getDescriptor() {
        CacheId id = CalculatableHelper.getId(calculatable.getClass());
        return CacheFactory.getProvider().getDescriptor(id);
    }

    @Override
    public String toString() {
        return getDescriptor() + ": " + owner;
    }
}