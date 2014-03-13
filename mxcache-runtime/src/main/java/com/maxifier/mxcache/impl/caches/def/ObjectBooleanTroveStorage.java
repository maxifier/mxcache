/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import gnu.trove.*;

import com.maxifier.mxcache.storage.*;

/**
 * ObjectBooleanTroveStorage<E>
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PTroveStorage.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ObjectBooleanTroveStorage<E> extends TObjectByteHashMap<E> implements ObjectBooleanStorage<E> {
    public ObjectBooleanTroveStorage() {
    }

    public ObjectBooleanTroveStorage(TObjectHashingStrategy<E> strategy) {
        super(strategy);
    }

    @Override
    public boolean isCalculated(E o) {
        return super.contains(o);
    }

    @Override
    public boolean load(E o) {
        return super.get(o) != 0;
    }

    @Override
    public void save(E o, boolean t) {
        put(o, (byte)(t? 1 : 0));
    }
}