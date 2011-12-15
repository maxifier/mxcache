package com.maxifier.mxcache.impl.caches.storage;

import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.caches.abs.*;
import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.impl.resource.*;
import com.maxifier.mxcache.storage.*;

import com.maxifier.mxcache.interfaces.Statistics;
import com.maxifier.mxcache.interfaces.StatisticsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
public class StorageByteFloatCacheImpl extends AbstractByteFloatCache implements StorageHolder<ByteFloatStorage> {
    private static final long serialVersionUID = 100L;

    private ByteFloatStorage storage;

    public StorageByteFloatCacheImpl(Object owner, ByteFloatCalculatable calculatable, @NotNull DependencyNode node, @NotNull MutableStatistics statistics) {
        super(owner, calculatable, node, statistics);
    }

    @Override
    public void setStorage(@NotNull ByteFloatStorage storage) {
        if (this.storage != null) {
            throw new UnsupportedOperationException("Storage already set");
        }
        this.storage = storage;
    }

    @Override
    public boolean isCalculated(byte key) {
        return storage.isCalculated(key);
    }

    @Override
    public float load(byte key) {
        return storage.load(key);
    }

    @Override
    public void save(byte key, float value) {
        storage.save(key, value);
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