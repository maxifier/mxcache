/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.abs.elementlocked;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.exceptions.*;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.CacheId;
import com.maxifier.mxcache.impl.CalculatableHelper;
import com.maxifier.mxcache.impl.resource.*;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.storage.elementlocked.*;

import java.util.concurrent.locks.Lock;


/**
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PCache.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public abstract class AbstractDoubleCache extends AbstractElementLockedCache implements DoubleCache, ObjectElementLockedStorage {
    private final DoubleCalculatable calculatable;

    public AbstractDoubleCache(Object owner, DoubleCalculatable calculatable, MutableStatistics statistics) {
        super(owner, statistics);
        this.calculatable = calculatable;
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public double getOrCreate() {
        if (DependencyTracker.isBypassCaches()) {
            return calculatable.calculate(owner);
        } else {
            Lock lock = getLock();
            if (lock != null) {
                lock.lock();
            }
            try {
                Object v = load();
                ExceptionHelper.throwIfExceptionRecordNotExpired(v);
                if (v != UNDEFINED) {
                    DependencyTracker.mark(getDependencyNode());
                    hit();
                    return (Double)v;
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
                                v = load();
                                ExceptionHelper.throwIfExceptionRecordNotExpired(v);
                                if (v != UNDEFINED) {
                                    hit();
                                    return (Double)v;
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

    @SuppressWarnings({ "unchecked" })
    protected double create() {
        long start = System.nanoTime();
        try {
            int retry = 0;
            // retry on exception loop
            while (true) {
                try {
                    double t = calculatable.calculate(owner);
                    // successful invocation => just store the value and return
                    save(t);
                    return t;
                } catch (Exception e) {
                    // We catch Exception here, but not Error and not Throwable.
                    // this is because in case of Error we are likely have no chance even to save
                    // an ExceptionRecord to a storage, so don't even try to do so.
                    // For example in case of OOM (out of memory) it may be impossible to create
                    // even a single new object.
                    CacheExceptionHandler exceptionHandler = getDescriptor().getExceptionHandler();
                    switch (exceptionHandler.getAction(retry, e)) {
                        case RETRY:
                            retry++;
                            continue;
                        case REMEMBER_AND_RETHROW:
                            save(new ExceptionRecord(e, exceptionHandler.getRememberExceptionExpirationTimestamp(e)));
                            // fall through
                        case RETHROW:
                        default:
                            // this method always throws an exception
                            ExceptionHelper.throwCheckedExceptionHack(e);
                            break;
                    }
                }
            }
        } finally {
            // record calculation time even if calculation fails
            long end = System.nanoTime();
            miss(end - start);
        }
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