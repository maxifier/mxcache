/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import gnu.trove.*;

import com.maxifier.mxcache.storage.*;

/**
 * ObjectDoubleTroveStorage<E>
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PTroveStorage.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ObjectDoubleTroveStorage<E> extends TObjectDoubleHashMap<E> implements ObjectDoubleStorage<E> {
    public ObjectDoubleTroveStorage() {
    }

    public ObjectDoubleTroveStorage(TObjectHashingStrategy<E> strategy) {
        super(strategy);
    }

    @Override
    public boolean isCalculated(E o) {
        return super.contains(o);
    }

    @Override
    public double load(E o) {
        return super.get(o);
    }

    @Override
    public void save(E o, double t) {
        put(o, t);
    }
}