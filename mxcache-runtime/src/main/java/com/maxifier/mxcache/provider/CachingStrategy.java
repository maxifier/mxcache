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
     * @param descriptor descriptor of the cache
     * @param <T> cache owner type
     * @return cache manager for given cache
     */
    @Nonnull
    <T> CacheManager<T> getManager(CacheContext context, CacheDescriptor<T> descriptor);
}
