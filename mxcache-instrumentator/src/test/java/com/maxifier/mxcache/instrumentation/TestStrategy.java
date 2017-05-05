/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
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
    public CacheManager getManager(CacheContext context, Class<?> ownerClass, CacheDescriptor descriptor) {
        return new NullCacheManager(ownerClass, descriptor);
    }
}
