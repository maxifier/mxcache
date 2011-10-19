package com.maxifier.mxcache.util;

import gnu.trove.THashMap;
import gnu.trove.TObjectIdentityHashingStrategy;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 01.05.2010
 * Time: 9:56:15
 */
@SuppressWarnings ({ "unchecked" })
public class TIdentityHashMap<K, V> extends THashMap<K, V> {
    private static final TObjectIdentityHashingStrategy STRATEGY = new TObjectIdentityHashingStrategy();

    public TIdentityHashMap() {
        super(STRATEGY);
    }

    public TIdentityHashMap(int initialCapacity) {
        super(initialCapacity, STRATEGY);
    }

    public TIdentityHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor, STRATEGY);
    }

    public TIdentityHashMap(Map<K, V> kvMap) {
        super(kvMap, STRATEGY);
    }
}
