package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.BooleanBooleanStorage;

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