/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.util;

import gnu.trove.map.hash.TCustomHashMap;
import gnu.trove.strategy.IdentityHashingStrategy;

import java.util.Map;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class TIdentityHashMap<K, V> extends TCustomHashMap<K, V> {
    public TIdentityHashMap() {
        super(IdentityHashingStrategy.INSTANCE);
    }

    public TIdentityHashMap(int initialCapacity) {
        super(IdentityHashingStrategy.INSTANCE, initialCapacity);
    }

    public TIdentityHashMap(int initialCapacity, float loadFactor) {
        super(IdentityHashingStrategy.INSTANCE, initialCapacity, loadFactor);
    }

    public TIdentityHashMap(Map<K, V> kvMap) {
        super(IdentityHashingStrategy.INSTANCE, kvMap);
    }
}
