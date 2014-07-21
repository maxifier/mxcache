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
public class LongIntTroveStorage extends gnu.trove.map.hash.TLongIntHashMap implements LongIntStorage {
    @Override
    public boolean isCalculated(long o) {
        return super.contains(o);
    }

    @Override
    public int load(long o) {
        return super.get(o);
    }

    @Override
    public void save(long o, int t) {
        put(o, t);
    }
}