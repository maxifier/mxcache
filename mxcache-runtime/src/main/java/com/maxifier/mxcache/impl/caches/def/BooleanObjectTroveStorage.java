/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.BooleanObjectStorage;

/**
 * BooleanObjectTroveStorage
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class BooleanObjectTroveStorage implements BooleanObjectStorage {
    private Object trueValue = UNDEFINED;
    private Object falseValue = UNDEFINED;

    @Override
    public Object load(boolean o) {
        return o ? trueValue : falseValue;
    }

    @Override
    public void save(boolean o, Object t) {
        if (o) {
            trueValue = t;
        } else {
            falseValue = t;
        }
    }

    @Override
    public void clear() {
        trueValue = UNDEFINED;
        falseValue = UNDEFINED;
    }

    @Override
    public int size() {
        int res = 0;
        if (trueValue != UNDEFINED) {
            res++;
        }
        if (falseValue != UNDEFINED) {
            res++;
        }
        return res;
    }
}