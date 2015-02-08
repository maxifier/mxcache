/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.provider;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.context.CacheContext;
import javax.annotation.Nullable;

/**
 * Every CacheManager corresponds to exactly one @Cache method. It holds an associated CacheDescriptor.
 *
 * CacheManagers are created for each combination of owner class, method and context.
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface CacheManager<T> {
    Class<?> getOwnerClass();

    /**
     * @return descriptor for the cache
     */
    CacheDescriptor<T> getDescriptor();

    /**
     * Creates a new instance of cache.
     * @param owner an instance of class that has cached method.
     * @return created cache instance. Should match key type and value type of cache.
     */
    Cache createCache(@Nullable T owner);

    /**
     * @return implementation details, e.g. class name of storage/cache
     */
    String getImplementationDetails();

    CacheContext getContext();
}
