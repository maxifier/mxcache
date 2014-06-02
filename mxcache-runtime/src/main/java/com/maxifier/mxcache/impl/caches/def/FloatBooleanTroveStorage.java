/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.*;

/**
 * FloatBooleanTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PTroveStorage.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class FloatBooleanTroveStorage extends gnu.trove.map.hash.TFloatByteHashMap implements FloatBooleanStorage {
    @Override
    public boolean isCalculated(float o) {
        return super.contains(o);
    }

    @Override
    public boolean load(float o) {
        return super.get(o) != 0;
    }

    @Override
    public void save(float o, boolean t) {
        put(o, (byte)(t? 1 : 0));
    }
}