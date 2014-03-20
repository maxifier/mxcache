/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.storage.elementlocked;

import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.caches.storage.StorageHolder;
import com.maxifier.mxcache.impl.caches.abs.elementlocked.*;
import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.storage.elementlocked.*;

import com.maxifier.mxcache.interfaces.Statistics;
import com.maxifier.mxcache.interfaces.StatisticsHolder;

import javax.annotation.Nonnull;

import javax.annotation.Nullable;

import java.util.concurrent.locks.Lock;

/**
 * StorageByteByteCacheImpl
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM StorageP2PCache.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class StorageByteByteCacheImpl extends AbstractByteByteCache implements StorageHolder<ByteByteElementLockedStorage> {
    private ByteByteElementLockedStorage storage;

    public StorageByteByteCacheImpl(Object owner, ByteByteCalculatable calculatable, @Nonnull MutableStatistics statistics) {
        super(owner, calculatable, statistics);
    }

    @Override
    public void setStorage(@Nonnull ByteByteElementLockedStorage storage) {
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
    public byte load(byte key) {
        return storage.load(key);
    }

    @Override
    public void save(byte key, byte value) {
        storage.save(key, value);
    }

    @Override
    public void lock(byte key) {
        storage.lock(key);
    }

    @Override
    public void unlock(byte key) {
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