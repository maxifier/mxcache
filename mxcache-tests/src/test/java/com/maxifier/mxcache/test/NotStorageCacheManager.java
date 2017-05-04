/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
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
public class NotStorageCacheManager extends AbstractCacheManager {
    public NotStorageCacheManager(CacheContext context, Class<?> ownerClass, CacheDescriptor descriptor) {
        super(context, ownerClass, descriptor);
    }

    @Nonnull
    @Override
    protected Cache createCache(Object owner, DependencyNode dependencyNode, MutableStatistics statistics) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        return new TestCache(owner, statistics, getDescriptor());
    }

    @Override
    public String getImplementationDetails() {
        return null;
    }
}
