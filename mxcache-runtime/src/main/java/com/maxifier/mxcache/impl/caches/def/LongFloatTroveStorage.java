/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.*;

/**
 * LongFloatTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PTroveStorage.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class LongFloatTroveStorage extends gnu.trove.map.hash.TLongFloatHashMap implements LongFloatStorage {
    @Override
    public boolean isCalculated(long o) {
        return super.contains(o);
    }

    @Override
    public float load(long o) {
        return super.get(o);
    }

    @Override
    public void save(long o, float t) {
        put(o, t);
    }
}