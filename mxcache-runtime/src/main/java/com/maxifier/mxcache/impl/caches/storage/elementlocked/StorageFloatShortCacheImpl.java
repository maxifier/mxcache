package com.maxifier.mxcache.impl.caches.storage.elementlocked;

import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.caches.storage.StorageHolder;
import com.maxifier.mxcache.impl.caches.abs.elementlocked.*;
import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.impl.resource.*;
import com.maxifier.mxcache.storage.elementlocked.*;

import com.maxifier.mxcache.interfaces.Statistics;
import com.maxifier.mxcache.interfaces.StatisticsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.Lock;

/**
 * Project: Maxifier
 * Created by: Yakoushin Andrey
 * Date: 15.02.2010
 * Time: 13:54:51
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
 */
public class StorageFloatShortCacheImpl extends AbstractFloatShortCache implements StorageHolder<FloatShortElementLockedStorage> {
    private FloatShortElementLockedStorage storage;

    public StorageFloatShortCacheImpl(Object owner, FloatShortCalculatable calculatable, @NotNull DependencyNode node, @NotNull MutableStatistics statistics) {
        super(owner, calculatable, node, statistics);
    }

    @Override
    public void setStorage(@NotNull FloatShortElementLockedStorage storage) {
        if (this.storage != null) {
            throw new UnsupportedOperationException("Storage already set");
        }
        this.storage = storage;
    }

    @Override
    public boolean isCalculated(float key) {
        return storage.isCalculated(key);
    }

    @Override
    public short load(float key) {
        return storage.load(key);
    }

    @Override
    public void save(float key, short value) {
        storage.save(key, value);
    }

    @Override
    public void lock(float key) {
        storage.lock(key);
    }

    @Override
    public void unlock(float key) {
        storage.unlock(key);
    }

    @Override
    public Lock getLock() {
        return storage.getLock();
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