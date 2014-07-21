/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.*;

/**
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PTroveStorage.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class DoubleIntTroveStorage extends gnu.trove.map.hash.TDoubleIntHashMap implements DoubleIntStorage {
    @Override
    public boolean isCalculated(double o) {
        return super.contains(o);
    }

    @Override
    public int load(double o) {
        return super.get(o);
    }

    @Override
    public void save(double o, int t) {
        put(o, t);
    }
}