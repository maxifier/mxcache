/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.clean;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.caches.CleaningNode;

import java.util.List;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class EmptyCleanable<T> implements Cleanable<T> {
    @Override
    public void appendStaticCachesTo(List<CleaningNode> list) {
    }

    @Override
    public Cache getStaticCache(int id) {
        return new CacheWithLock(null);
    }

    @Override
    public void appendInstanceCachesTo(List<CleaningNode> list, T o) {
    }

    @Override
    public Cache getInstanceCache(T o, int id) {
        return new CacheWithLock(null);
    }
}
