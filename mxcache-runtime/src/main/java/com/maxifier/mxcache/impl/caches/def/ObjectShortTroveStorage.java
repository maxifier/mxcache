/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import gnu.trove.map.custom_hash.TObjectShortCustomHashMap;
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
public class ObjectShortTroveStorage<E> extends TObjectShortCustomHashMap<E> implements ObjectShortStorage<E> {
    public ObjectShortTroveStorage() {
        //noinspection unchecked
        super(DEFAULT_HASHING_STRATEGY);
    }

    public ObjectShortTroveStorage(HashingStrategy<E> strategy) {
        super(strategy);
    }

    @Override
    public boolean isCalculated(E o) {
        return super.contains(o);
    }

    @Override
    public short load(E o) {
        return super.get(o);
    }

    @Override
    public void save(E o, short t) {
        put(o, t);
    }
}