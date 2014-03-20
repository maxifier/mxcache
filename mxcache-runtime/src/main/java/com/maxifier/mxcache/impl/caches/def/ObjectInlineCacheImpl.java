/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.caches.ObjectCalculatable;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.caches.abs.AbstractObjectCache;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ObjectInlineCacheImpl<T> extends AbstractObjectCache<T> {
    private volatile Object value = UNDEFINED;

    public ObjectInlineCacheImpl(Object owner, ObjectCalculatable<T> calculable, MutableStatistics statistics) {
        super(owner, calculable, statistics);
    }

    @Override
    public Object load() {
        return value;
    }

    @Override
    public void save(T v) {
        value = v;
    }

    @Override
    public void clear() {
        value = UNDEFINED;
    }

    @Override
    public int size() {
        return value == UNDEFINED ? 0 : 1;
    }
}
