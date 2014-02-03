/*
 * Copyright (c) 2008-2013 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.test;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.impl.AbstractCacheManager;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.provider.CacheDescriptor;

import javax.annotation.Nonnull;

import java.lang.reflect.InvocationTargetException;

/**
 * NotStorageCacheManager
 *
 * @author Elena Saymanina (elena.saymanina@maxifier.com) (06.06.13)
 */
public class NotStorageCacheManager<T> extends AbstractCacheManager<T> {
    public NotStorageCacheManager(CacheContext context, CacheDescriptor<T> descriptor) {
        super(context, descriptor);
    }

    @Nonnull
    @Override
    protected Cache createCache(Object owner, DependencyNode dependencyNode, MutableStatistics statistics) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        return new TestCache(owner, dependencyNode, statistics, getDescriptor());
    }

    @Override
    public String getImplementationDetails() {
        return null;
    }
}
