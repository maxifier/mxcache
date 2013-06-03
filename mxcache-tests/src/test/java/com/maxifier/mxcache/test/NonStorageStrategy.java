/*
 * Copyright (c) 2008-2013 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.test;

import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.CacheManager;
import com.maxifier.mxcache.provider.CachingStrategy;
import org.jetbrains.annotations.NotNull;

/**
 * NonStorageStrategy
 *
 * @author Elena Saymanina (elena.saymanina@maxifier.com) (06.06.13)
 */
public class NonStorageStrategy implements CachingStrategy {
    @NotNull
    @Override
    public <T> CacheManager<T> getManager(CacheContext context, CacheDescriptor<T> descriptor) {
        return new NotStorageCacheManager<T>(context, descriptor);
    }
}
