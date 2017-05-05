/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import com.maxifier.mxcache.context.CacheContext;
import gnu.trove.map.hash.THashMap;

import java.util.Map;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public abstract class AbstractCacheContext implements CacheContext {
    private final Map<ContextRelatedItem, Object> cache = new THashMap<ContextRelatedItem, Object>();

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <T> T getRelated(ContextRelatedItem<T> item) {
        return (T) cache.get(item);
    }

    @Override
    public synchronized <T> void setRelated(ContextRelatedItem<T> item, T value) {
        cache.put(item, value);
    }
}
