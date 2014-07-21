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
public class LongByteTroveStorage extends gnu.trove.map.hash.TLongByteHashMap implements LongByteStorage {
    @Override
    public boolean isCalculated(long o) {
        return super.contains(o);
    }

    @Override
    public byte load(long o) {
        return super.get(o);
    }

    @Override
    public void save(long o, byte t) {
        put(o, t);
    }
}