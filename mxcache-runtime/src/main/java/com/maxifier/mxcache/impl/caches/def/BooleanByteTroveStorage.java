package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.*;

/**
 * Project: Maxifier
 * Created by: Yakoushin Andrey
 * Date: 15.02.2010
 * Time: 13:54:51
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
 */

public class BooleanByteTroveStorage implements BooleanByteStorage {
    private byte trueValue;
    private byte falseValue;

    private boolean trueSet;
    private boolean falseSet;

    @Override
    public boolean isCalculated(boolean o) {
        return o ? trueSet : falseSet;
    }

    @Override
    public byte load(boolean o) {
        return o ? trueValue : falseValue;
    }

    @Override
    public void save(boolean o, byte t) {
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