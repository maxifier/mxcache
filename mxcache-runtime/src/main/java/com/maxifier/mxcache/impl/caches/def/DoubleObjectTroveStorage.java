/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import gnu.trove.*;

import com.maxifier.mxcache.storage.*;
import static com.maxifier.mxcache.impl.caches.def.TroveHelper.*;

/**
 * DoubleObjectTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM #SOURCE#
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class DoubleObjectTroveStorage<T> extends TDoubleObjectHashMap<Object> implements DoubleObjectStorage<T> {
    public DoubleObjectTroveStorage() {
    }

    public DoubleObjectTroveStorage(TDoubleHashingStrategy strategy) {
        super(strategy);        
    }

    @Override
    public Object load(double key) {
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
    public void save(double key, T value) {
        put(key, value == null ? NULL_REPLACEMENT : value);
    }
}
