/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.ehcache;

import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.StorageFactory;
import com.maxifier.mxcache.storage.Storage;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import javax.annotation.Nonnull;

/**
 * EhcacheStorageFactory
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class EhcacheStorageFactory<T> implements StorageFactory<T> {
    private final CacheManager cacheManager;
    private final CacheDescriptor descriptor;

    public EhcacheStorageFactory(CacheDescriptor<T> descriptor, CacheManager cacheManager) {
        this.descriptor = descriptor;
        this.cacheManager = cacheManager;
    }

    @Nonnull
    @Override
    public Storage createStorage(T owner) {
        String name = descriptor.getCacheName();
        Cache cache = cacheManager.getCache(name);
        return new EhcacheStorage(cache);
    }

    @Override
    public String getImplementationDetails() {
        return "EhCache";
    }
}
