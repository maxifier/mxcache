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
public class FloatFloatTroveStorage extends gnu.trove.map.hash.TFloatFloatHashMap implements FloatFloatStorage {
    @Override
    public boolean isCalculated(float o) {
        return super.contains(o);
    }

    @Override
    public float load(float o) {
        return super.get(o);
    }

    @Override
    public void save(float o, float t) {
        put(o, t);
    }
}