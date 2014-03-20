/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.impl.NullCacheManager;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.CacheManager;
import com.maxifier.mxcache.provider.CachingStrategy;

import javax.annotation.Nonnull;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class TestStrategy implements CachingStrategy {
    @Nonnull
    @Override
    public <T> CacheManager<T> getManager(CacheContext context, CacheDescriptor<T> descriptor) {
        return new NullCacheManager<T>(descriptor);
    }
}
