/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.storage;

import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.caches.abs.*;
import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.storage.*;

import com.maxifier.mxcache.interfaces.Statistics;
import com.maxifier.mxcache.interfaces.StatisticsHolder;

import javax.annotation.Nonnull;

import javax.annotation.Nullable;

/**
 * StorageObjectCacheImpl<F>
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM StorageP2OCache.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class StorageObjectCacheImpl<F> extends AbstractObjectCache<F> implements StorageHolder<ObjectStorage<F>> {
    private static final long serialVersionUID = 100L;

    private ObjectStorage<F> storage;

    public StorageObjectCacheImpl(Object owner, ObjectCalculatable<F> calculatable, @Nonnull MutableStatistics statistics) {
        super(owner, calculatable, statistics);
    }

    @Override
    public void setStorage(@Nonnull ObjectStorage<F> storage) {
        if (this.storage != null) {
            throw new UnsupportedOperationException("Storage already set");
        }
        this.storage = storage;
    }

    @Override
    public Object load() {
        return storage.load();
    }

    @Override
    public void save(F value) {
        storage.save(value);
    }

    @Override
    public void clear() {
        storage.clear();
    }

    @Override
    public int size() {
        return storage.size();
    }

    @Nullable
    @Override
    public Statistics getStatistics() {
        if (storage instanceof StatisticsHolder) {
            return ((StatisticsHolder)storage).getStatistics();
        }
        return super.getStatistics();
    }
}