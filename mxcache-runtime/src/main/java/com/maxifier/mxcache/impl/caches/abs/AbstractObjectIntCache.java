/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.abs;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.exceptions.*;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.CacheId;
import com.maxifier.mxcache.impl.CalculatableHelper;
import com.maxifier.mxcache.impl.resource.*;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.storage.*;

/**
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PCache.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public abstract class AbstractObjectIntCache<E> extends AbstractCache implements ObjectIntCache<E>, ObjectObjectStorage<E> {
    private final ObjectIntCalculatable<E> calculatable;

    public AbstractObjectIntCache(Object owner, ObjectIntCalculatable<E> calculatable, MutableStatistics statistics) {
        super(owner, statistics);
        this.calculatable = calculatable;
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public int getOrCreate(E o) {
        if (DependencyTracker.isBypassCaches()) {
            return calculatable.calculate(owner, o);
        } else {
            preCheckDirty();
            lock();
            try {
                Object v = load(o);
                if (v != UNDEFINED) {
                    DependencyTracker.mark(getDependencyNode());
                    hit();
                    ExceptionHelper.throwIfExceptionRecordNotExpired(v);
                    return (Integer)v;
                }
                DependencyNode callerNode = DependencyTracker.track(getDependencyNode());
                try {
                    while(true) {
                        try {
                            return create(o);
                        } catch (ResourceOccupied e) {
                            if (callerNode != null) {
                                throw e;
                            } else {
                                unlock();
                                try {
                                    e.getResource().waitForEndOfModification();
                                } finally {
                                    lock();
                                }
                                v = load(o);
                                if (v != UNDEFINED) {
                                    hit();
                                    ExceptionHelper.throwIfExceptionRecordNotExpired(v);
                                    return (Integer)v;
                                }
                            }
                        }
                    }
                } finally {
                    DependencyTracker.exit(callerNode);
                }
            } finally {
                unlock();
                postCheckDirty();
            }
        }
    }

    @SuppressWarnings({ "unchecked" })
    protected int create(E o) {
        long start = System.nanoTime();
        try {
            int retry = 0;
            // retry on exception loop
            while (true) {
                try {
                    int t = calculatable.calculate(owner, o);
                    // successful invocation => just store the value and return
                    save(o, t);
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
                            save(o, new ExceptionRecord(e, exceptionHandler.getRememberExceptionExpirationTimestamp(e)));
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