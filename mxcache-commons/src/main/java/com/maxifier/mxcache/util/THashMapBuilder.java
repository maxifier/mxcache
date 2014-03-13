/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.util;

import gnu.trove.THashMap;

import java.util.Collections;
import java.util.Map;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class THashMapBuilder<K, V> {
    private final Map<K, V> map = new THashMap<K, V>();

    public THashMapBuilder<K, V> put(K k, V v) {
        map.put(k, v);
        return this;
    }

    public Map<K, V> toMap() {
        return Collections.unmodifiableMap(new THashMap<K, V>(map));
    }
}
