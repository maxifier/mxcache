/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import gnu.trove.*;

import com.maxifier.mxcache.storage.*;
import static com.maxifier.mxcache.impl.caches.def.TroveHelper.*;

/**
 * ByteObjectTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM #SOURCE#
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ByteObjectTroveStorage<T> extends TByteObjectHashMap<Object> implements ByteObjectStorage<T> {
    public ByteObjectTroveStorage() {
    }

    public ByteObjectTroveStorage(TByteHashingStrategy strategy) {
        super(strategy);        
    }

    @Override
    public Object load(byte key) {
        Object v = get(key);
        if (v == null) {
            return UNDEFINED;
        }
        if (v == NULL_REPLACEMENT) {
            return null;
        }
        return v;
    }

    @Override
    public void save(byte key, T value) {
        put(key, value == null ? NULL_REPLACEMENT : value);
    }
}
