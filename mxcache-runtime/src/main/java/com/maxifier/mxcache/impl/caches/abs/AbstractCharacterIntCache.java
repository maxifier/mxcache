/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
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
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PCache.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public abstract class AbstractCharacterIntCache extends AbstractCache implements CharacterIntCache, CharacterObjectStorage {
    private final CharacterIntCalculatable calculatable;

    public AbstractCharacterIntCache(Object owner, CharacterIntCalculatable calculatable, MutableStatistics statistics) {
        super(owner, statistics);
        this.calculatable = calculatable;
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public int getOrCreate(char o) {
        if (DependencyTracker.isBypassCaches()) {
            return calculatable.calculate(owner, o);
        } else {
            lock();
            try {
                Object v = load(o);
                if (v != UNDEFINED) {
                    DependencyTracker.mark(getDependencyNode());
                    hit();
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
            }
        }
    }

    @SuppressWarnings({ "unchecked" })
    protected int create(char o) {
        long start = System.nanoTime();
        int t = calculatable.calculate(owner, o);
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