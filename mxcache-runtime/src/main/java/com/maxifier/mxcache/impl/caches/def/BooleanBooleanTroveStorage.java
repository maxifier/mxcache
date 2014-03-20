/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.BooleanBooleanStorage;

/**
 * BooleanBooleanTroveStorage
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class BooleanBooleanTroveStorage implements BooleanBooleanStorage {
    private Boolean trueValue;
    private Boolean falseValue;

    @Override
    public boolean isCalculated(boolean o) {
        return (o ? trueValue : falseValue) != null;
    }

    @Override
    public boolean load(boolean o) {
        return o ? trueValue : falseValue;
    }

    @Override
    public void save(boolean o, boolean v) {
        if (o) {
            trueValue = v;
        } else {
            falseValue = v;
        }
    }

    @Override
    public void clear() {
        trueValue = null;
        falseValue = null;
    }

    @Override
    public int size() {
        int res = 0;
        if (trueValue != null) {
            res++;
        }
        if (falseValue != null) {
            res++;
        }
        return res;
    }
}