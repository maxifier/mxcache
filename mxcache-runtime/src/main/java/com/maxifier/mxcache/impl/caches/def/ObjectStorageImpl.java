/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.*;

/**
 * ObjectStorageImpl
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ObjectStorageImpl<T> implements ObjectStorage<T> {
    private volatile Object value = UNDEFINED;

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