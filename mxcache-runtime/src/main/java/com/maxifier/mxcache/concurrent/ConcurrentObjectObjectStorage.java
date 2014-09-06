/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.concurrent;

import com.maxifier.mxcache.PublicAPI;
import com.maxifier.mxcache.impl.caches.def.TroveHelper;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.StrategyProperty;
import com.maxifier.mxcache.storage.elementlocked.ObjectObjectElementLockedStorage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ConcurrentObjectObjectCache is cache storage implementation for {@link com.maxifier.mxcache.concurrent.ConcurrentCache}
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-09-06 15:25)
 */
public class ConcurrentObjectObjectStorage<K, V> implements ObjectObjectElementLockedStorage<K, V> {
    private static final float LOAD_FACTOR = 0.75f;

    // Read-write lock is used to guard overall locking on cache cleaning
    private final Lock readLock;
    private final Lock writeLock;

    private static final StrategyProperty<Integer> CONCURRENCY_LEVEL = StrategyProperty.create(
            "concurrency-level",
            Integer.class,
            ConcurrentCache.DEFAULT_CONCURRENCY_LEVEL,
            ConcurrentCache.class,
            "concurrencyLevel");

    private static final StrategyProperty<Integer> INITIAL_CAPACITY = StrategyProperty.create(
            "initial-capacity",
            Integer.class,
            ConcurrentCache.DEFAULT_INITIAL_CAPACITY,
            ConcurrentCache.class,
            "initialCapacity");

    private final ConcurrentHashMap<K, Object> concurrentHashMap;

    @PublicAPI
    public ConcurrentObjectObjectStorage(CacheDescriptor<?> descriptor) {
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        readLock = readWriteLock.readLock();
        writeLock = readWriteLock.writeLock();

        concurrentHashMap = new ConcurrentHashMap<K, Object>(
                descriptor.getProperty(INITIAL_CAPACITY),
                LOAD_FACTOR,
                descriptor.getProperty(CONCURRENCY_LEVEL));
    }

    @Override
    public void lock(K key) {
        readLock.lock();
    }

    @Override
    public void unlock(K key) {
        readLock.unlock();
    }

    @Override
    public Lock getLock() {
        return writeLock;
    }

    @Override
    public void clear() {
        concurrentHashMap.clear();
    }

    @Override
    public int size() {
        return concurrentHashMap.size();
    }

    @Override
    public Object load(K o) {
        Object res = concurrentHashMap.get(o);
        if (res == null) {
            return UNDEFINED;
        }
        return res == TroveHelper.NULL_REPLACEMENT ? null : res;
    }

    @Override
    public void save(K o, V v) {
        concurrentHashMap.put(o, v == null ? TroveHelper.NULL_REPLACEMENT : v);
    }
}
