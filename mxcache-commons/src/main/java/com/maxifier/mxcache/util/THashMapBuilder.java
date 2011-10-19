package com.maxifier.mxcache.util;

import gnu.trove.THashMap;

import java.util.Collections;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 19.10.2010
 * Time: 11:46:45
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
