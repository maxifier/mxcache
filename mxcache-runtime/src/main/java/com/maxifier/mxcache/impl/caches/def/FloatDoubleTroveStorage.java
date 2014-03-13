/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import gnu.trove.*;

import com.maxifier.mxcache.storage.*;

/**
 * FloatDoubleTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PTroveStorage.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class FloatDoubleTroveStorage extends TFloatDoubleHashMap implements FloatDoubleStorage {
    public FloatDoubleTroveStorage() {
    }

    public FloatDoubleTroveStorage(TFloatHashingStrategy strategy) {
        super(strategy);
    }

    @Override
    public boolean isCalculated(float o) {
        return super.contains(o);
    }

    @Override
    public double load(float o) {
        return super.get(o);
    }

    @Override
    public void save(float o, double t) {
        put(o, t);
    }
}