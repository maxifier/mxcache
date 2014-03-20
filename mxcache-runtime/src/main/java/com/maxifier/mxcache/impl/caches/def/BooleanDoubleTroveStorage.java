/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.*;

/**
 * BooleanDoubleTroveStorage - default cache storage for caches that has boolean key.
 * Though "Trove" is used in name, but it's actually not a map, it's just a set of two values.
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM #SOURCE#
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class BooleanDoubleTroveStorage implements BooleanDoubleStorage {
    private double trueValue;
    private double falseValue;

    private boolean trueSet;
    private boolean falseSet;

    @Override
    public boolean isCalculated(boolean o) {
        return o ? trueSet : falseSet;
    }

    @Override
    public double load(boolean o) {
        return o ? trueValue : falseValue;
    }

    @Override
    public void save(boolean o, double t) {
        if (o) {
            trueValue = t;
            trueSet = true;
        } else {
            falseValue = t;
            falseSet = true;
        }
    }

    @Override
    public void clear() {
        trueSet = false;
        falseSet = false;
    }

    @Override
    public int size() {
        int res = 0;
        if (trueSet) {
            res++;
        }
        if (falseSet) {
            res++;
        }
        return res;
    }
}