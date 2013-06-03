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
 * Project: Maxifier
 * Created by: Yakoushin Andrey
 * Date: 15.02.2010
 * Time: 13:54:51
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
 */
public abstract class AbstractShortCache extends AbstractElementLockedCache implements ShortCache, ShortElementLockedStorage {
    private final ShortCalculatable calculatable;

    public AbstractShortCache(Object owner, ShortCalculatable calculatable, MutableStatistics statistics) {
        super(owner, statistics);
        this.calculatable = calculatable;
    }

    @Override
    public short getOrCreate() {
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

    private short create() {
        long start = System.nanoTime();
        short t = calculatable.calculate(owner);
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