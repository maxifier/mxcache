/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import gnu.trove.*;

import com.maxifier.mxcache.storage.*;

/**
 * ObjectShortTroveStorage<E>
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PTroveStorage.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ObjectShortTroveStorage<E> extends TObjectShortHashMap<E> implements ObjectShortStorage<E> {
    public ObjectShortTroveStorage() {
    }

    public ObjectShortTroveStorage(TObjectHashingStrategy<E> strategy) {
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