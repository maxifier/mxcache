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
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM StorageP2PCache.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class StorageCharacterShortCacheImpl extends AbstractCharacterShortCache implements StorageHolder<CharacterShortElementLockedStorage> {
    private CharacterShortElementLockedStorage storage;

    public StorageCharacterShortCacheImpl(Object owner, CharacterShortCalculatable calculatable, @Nonnull MutableStatistics statistics) {
        super(owner, calculatable, statistics);
    }

    @Override
    public void setStorage(@Nonnull CharacterShortElementLockedStorage storage) {
        if (this.storage != null) {
            throw new UnsupportedOperationException("Storage already set");
        }
        this.storage = storage;
    }

    @Override
    public boolean isCalculated(char key) {
        return storage.isCalculated(key);
    }

    @Override
    public short load(char key) {
        return storage.load(key);
    }

    @Override
    public void save(char key, short value) {
        storage.save(key, value);
    }

    @Override
    public void lock(char key) {
        storage.lock(key);
    }

    @Override
    public void unlock(char key) {
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