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
public class ByteIntTroveStorage extends gnu.trove.map.hash.TByteIntHashMap implements ByteIntStorage {
    @Override
    public boolean isCalculated(byte o) {
        return super.contains(o);
    }

    @Override
    public int load(byte o) {
        return super.get(o);
    }

    @Override
    public void save(byte o, int t) {
        put(o, t);
    }
}