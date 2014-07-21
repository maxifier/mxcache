/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;
import gnu.trove.strategy.HashingStrategy;
import com.maxifier.mxcache.storage.*;

/**
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM O2PTroveStorage.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ObjectIntTroveStorage<E> extends TObjectIntCustomHashMap<E> implements ObjectIntStorage<E> {
    public ObjectIntTroveStorage() {
        //noinspection unchecked
        super(DEFAULT_HASHING_STRATEGY);
    }

    public ObjectIntTroveStorage(HashingStrategy<E> strategy) {
        super(strategy);
    }

    @Override
    public boolean isCalculated(E o) {
        return super.contains(o);
    }

    @Override
    public int load(E o) {
        return super.get(o);
    }

    @Override
    public void save(E o, int t) {
        put(o, t);
    }
}