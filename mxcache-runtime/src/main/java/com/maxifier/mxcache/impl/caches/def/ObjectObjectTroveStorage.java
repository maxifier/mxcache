/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.ObjectObjectStorage;
import gnu.trove.map.hash.TCustomHashMap;
import gnu.trove.strategy.HashingStrategy;

/**
 * ObjectObjectTroveStorage
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ObjectObjectTroveStorage<K> extends TCustomHashMap<K, Object> implements ObjectObjectStorage<K> {
    public ObjectObjectTroveStorage() {
        //noinspection unchecked
        super(DEFAULT_HASHING_STRATEGY);
    }

    public ObjectObjectTroveStorage(HashingStrategy<K> strategy) {
        super(strategy);        
    }

    @Override
    public Object load(K key) {
        Object v = get(key);
        if (v == null) {
            return UNDEFINED;
        }
        if (v == TroveHelper.NULL_REPLACEMENT) {
            return null;
        }
        return v;
    }

    @Override
    public void save(K key, Object value) {
        put(key, value == null ? TroveHelper.NULL_REPLACEMENT : value);
    }
}
