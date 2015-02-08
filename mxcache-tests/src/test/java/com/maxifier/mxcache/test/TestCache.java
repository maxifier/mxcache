/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.test;

import com.maxifier.mxcache.caches.LongCache;
import com.maxifier.mxcache.caches.LongCalculatable;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.interfaces.Statistics;
import com.maxifier.mxcache.provider.CacheDescriptor;

/**
 * TestCache
 *
 * @author Elena Saymanina (elena.saymanina@maxifier.com) (06.06.13)
 */
public class TestCache<T> implements LongCache {
    private final Object owner;
    private final MutableStatistics statistics;
    private final CacheDescriptor<T> descriptor;
    private final LongCalculatable calculatable;

    public TestCache(Object owner, MutableStatistics statistics, CacheDescriptor<T> descriptor) {
        this.owner = owner;
        this.statistics = statistics;
        this.descriptor = descriptor;
        this.calculatable = (LongCalculatable) descriptor.getCalculable();
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public Statistics getStatistics() {
        return statistics;
    }

    @Override
    public CacheDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public void setDependencyNode(DependencyNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DependencyNode getDependencyNode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getOrCreate() {
        return calculatable.calculate(owner);
    }

    @Override
    public void invalidate() {

    }
}
