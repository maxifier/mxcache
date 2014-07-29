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
public class DoubleByteTroveStorage extends gnu.trove.map.hash.TDoubleByteHashMap implements DoubleByteStorage {
    @Override
    public boolean isCalculated(double o) {
        return super.contains(o);
    }

    @Override
    public byte load(double o) {
        return super.get(o);
    }

    @Override
    public void save(double o, byte t) {
        put(o, t);
    }
}