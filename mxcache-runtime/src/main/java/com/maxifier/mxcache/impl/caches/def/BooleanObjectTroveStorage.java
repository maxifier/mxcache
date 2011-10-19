package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.BooleanObjectStorage;

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

public class BooleanObjectTroveStorage<V> implements BooleanObjectStorage<V> {
    private Object trueValue = UNDEFINED;
    private Object falseValue = UNDEFINED;

    @Override
    public Object load(boolean o) {
        return o ? trueValue : falseValue;
    }

    @Override
    public void save(boolean o, V t) {
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