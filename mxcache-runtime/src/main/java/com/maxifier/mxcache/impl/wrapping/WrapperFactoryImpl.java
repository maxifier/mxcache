/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.wrapping;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.caches.Calculable;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.caches.storage.StorageHolder;
import com.maxifier.mxcache.storage.Storage;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
class WrapperFactoryImpl implements WrapperFactory {
    private final Constructor<? extends Cache> constructor;

    public WrapperFactoryImpl(Constructor<? extends Cache> constructor) {
        this.constructor = constructor;
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public Cache wrap(Object owner, Calculable calculable, Storage storage, MutableStatistics statistics) {
        try {
            Cache cache = constructor.newInstance(owner, calculable, statistics);
            ((StorageHolder)cache).setStorage(storage);
            return cache;
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }
}
