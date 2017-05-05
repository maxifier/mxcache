/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.test;

import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.CacheManager;
import com.maxifier.mxcache.provider.CachingStrategy;

import javax.annotation.Nonnull;

/**
 * NonStorageStrategy
 *
 * @author Elena Saymanina (elena.saymanina@maxifier.com) (06.06.13)
 */
public class NonStorageStrategy implements CachingStrategy {
    @Nonnull
    @Override
    public CacheManager getManager(CacheContext context, Class<?> ownerClass, CacheDescriptor descriptor) {
        return new NotStorageCacheManager(context, ownerClass, descriptor);
    }
}
