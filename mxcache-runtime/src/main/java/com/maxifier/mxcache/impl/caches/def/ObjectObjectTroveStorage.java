package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.ObjectObjectStorage;
import gnu.trove.THashMap;
import gnu.trove.TObjectHashingStrategy;

/**
 * Project: Maxifier
 * Created by: Yakoushin Andrey
 * Date: 15.02.2010
 * Time: 13:29:47
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
 */
public class ObjectObjectTroveStorage<K, V> extends THashMap<K, Object> implements ObjectObjectStorage<K, V> {
    public ObjectObjectTroveStorage() {
    }

    public ObjectObjectTroveStorage(TObjectHashingStrategy<K> strategy) {
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
    public void save(K key, V value) {
        put(key, value == null ? TroveHelper.NULL_REPLACEMENT : value);
    }
}
