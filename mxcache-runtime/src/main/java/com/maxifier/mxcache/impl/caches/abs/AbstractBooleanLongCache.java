package com.maxifier.mxcache.impl.caches.abs;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.CacheId;
import com.maxifier.mxcache.impl.CalculatableHelper;
import com.maxifier.mxcache.impl.resource.*;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.storage.*;

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
public abstract class AbstractBooleanLongCache extends AbstractCache implements BooleanLongCache, BooleanLongStorage {
    private final BooleanLongCalculatable calculatable;

    public AbstractBooleanLongCache(Object owner, BooleanLongCalculatable calculatable, MutableStatistics statistics) {
        super(owner, statistics);
        this.calculatable = calculatable;
    }

    @Override
    public long getOrCreate(boolean o) {
        if (DependencyTracker.isBypassCaches()) {
            return calculatable.calculate(owner, o);
        } else {
            lock();
            try {
                if (isCalculated(o)) {
                    DependencyTracker.mark(getDependencyNode());
                    hit();
                    return load(o);
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
                                if (isCalculated(o)) {
                                    hit();
                                    return load(o);
                                }
                            }
                        }
                    }
                } finally {
                    DependencyTracker.exit(callerNode);
                }
            } finally {
                unlock();
            }
        }
    }

    private long create(boolean o) {
        long start = System.nanoTime();
        long t = calculatable.calculate(owner, o);
        long end = System.nanoTime();
        miss(end - start);
        save(o, t);
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