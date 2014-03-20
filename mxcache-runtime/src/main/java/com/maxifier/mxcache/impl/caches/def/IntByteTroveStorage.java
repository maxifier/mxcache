/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import gnu.trove.*;

import com.maxifier.mxcache.storage.*;

/**
 * IntByteTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PTroveStorage.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class IntByteTroveStorage extends TIntByteHashMap implements IntByteStorage {
    public IntByteTroveStorage() {
    }

    public IntByteTroveStorage(TIntHashingStrategy strategy) {
        super(strategy);
    }

    @Override
    public boolean isCalculated(int o) {
        return super.contains(o);
    }

    @Override
    public byte load(int o) {
        return super.get(o);
    }

    @Override
    public void save(int o, byte t) {
        put(o, t);
    }
}