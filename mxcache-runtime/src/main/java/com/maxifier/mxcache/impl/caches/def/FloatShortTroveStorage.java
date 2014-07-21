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
public class FloatShortTroveStorage extends gnu.trove.map.hash.TFloatShortHashMap implements FloatShortStorage {
    @Override
    public boolean isCalculated(float o) {
        return super.contains(o);
    }

    @Override
    public short load(float o) {
        return super.get(o);
    }

    @Override
    public void save(float o, short t) {
        put(o, t);
    }
}