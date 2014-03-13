/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.provider;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.context.CacheContext;
import javax.annotation.Nullable;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface CacheManager<T> {
    /**
     * @return дескриптор, переданный при создании
     */
    CacheDescriptor<T> getDescriptor();

    /**
     * Создает экземпляр кэша
     * @param owner вдаделец кэша
     * @return экземпляр кэша
     */
    Cache createCache(@Nullable T owner);

    /**
     * @return implementation details, e.g. class name of storage/cache
     */
    String getImplementationDetails();

    CacheContext getContext();
}
