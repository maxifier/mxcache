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
public class ShortDoubleTroveStorage extends gnu.trove.map.hash.TShortDoubleHashMap implements ShortDoubleStorage {
    @Override
    public boolean isCalculated(short o) {
        return super.contains(o);
    }

    @Override
    public double load(short o) {
        return super.get(o);
    }

    @Override
    public void save(short o, double t) {
        put(o, t);
    }
}