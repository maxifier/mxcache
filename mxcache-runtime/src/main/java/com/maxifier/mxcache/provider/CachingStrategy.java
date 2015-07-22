/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.provider;

import com.maxifier.mxcache.context.CacheContext;

import javax.annotation.Nonnull;

/**
 * CachingStrategy is a factory for CacheManager's.
 *
 * @see com.maxifier.mxcache.provider.CacheManager
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface CachingStrategy {
    /**
     * @param context cache context
     * @param ownerClass actual class that holds a cache (if cached method is inherited in a subclass, this parameter
     *                   will contain actual subclass). For static caches it is always exact declaring class.
     * @param descriptor descriptor of the cache
     * @return cache manager for given cache
     */
    @Nonnull
    CacheManager getManager(CacheContext context, Class<?> ownerClass, CacheDescriptor descriptor);
}
