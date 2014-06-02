/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.*;

/**
 * ShortIntTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PTroveStorage.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ShortIntTroveStorage extends gnu.trove.map.hash.TShortIntHashMap implements ShortIntStorage {
    @Override
    public boolean isCalculated(short o) {
        return super.contains(o);
    }

    @Override
    public int load(short o) {
        return super.get(o);
    }

    @Override
    public void save(short o, int t) {
        put(o, t);
    }
}