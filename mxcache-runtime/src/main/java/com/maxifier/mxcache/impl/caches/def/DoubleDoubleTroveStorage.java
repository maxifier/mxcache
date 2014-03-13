/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import gnu.trove.*;

import com.maxifier.mxcache.storage.*;

/**
 * DoubleDoubleTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PTroveStorage.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class DoubleDoubleTroveStorage extends TDoubleDoubleHashMap implements DoubleDoubleStorage {
    public DoubleDoubleTroveStorage() {
    }

    public DoubleDoubleTroveStorage(TDoubleHashingStrategy strategy) {
        super(strategy);
    }

    @Override
    public boolean isCalculated(double o) {
        return super.contains(o);
    }

    @Override
    public double load(double o) {
        return super.get(o);
    }

    @Override
    public void save(double o, double t) {
        put(o, t);
    }
}