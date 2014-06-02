/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import gnu.trove.map.custom_hash.TObjectShortCustomHashMap;
import gnu.trove.strategy.HashingStrategy;
import com.maxifier.mxcache.storage.*;

/**
 * ObjectCharacterTroveStorage<E>
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM O2PTroveStorage.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ObjectCharacterTroveStorage<E> extends TObjectShortCustomHashMap<E> implements ObjectCharacterStorage<E> {
    public ObjectCharacterTroveStorage() {
        //noinspection unchecked
        super(DEFAULT_HASHING_STRATEGY);
    }

    public ObjectCharacterTroveStorage(HashingStrategy<E> strategy) {
        super(strategy);
    }

    @Override
    public boolean isCalculated(E o) {
        return super.contains(o);
    }

    @Override
    public char load(E o) {
        return (char)super.get(o);
    }

    @Override
    public void save(E o, char t) {
        put(o, (short)t);
    }
}