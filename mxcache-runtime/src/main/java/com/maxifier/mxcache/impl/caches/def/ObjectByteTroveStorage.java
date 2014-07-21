/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import gnu.trove.map.custom_hash.TObjectByteCustomHashMap;
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
public class ObjectByteTroveStorage<E> extends TObjectByteCustomHashMap<E> implements ObjectByteStorage<E> {
    public ObjectByteTroveStorage() {
        //noinspection unchecked
        super(DEFAULT_HASHING_STRATEGY);
    }

    public ObjectByteTroveStorage(HashingStrategy<E> strategy) {
        super(strategy);
    }

    @Override
    public boolean isCalculated(E o) {
        return super.contains(o);
    }

    @Override
    public byte load(E o) {
        return super.get(o);
    }

    @Override
    public void save(E o, byte t) {
        put(o, t);
    }
}