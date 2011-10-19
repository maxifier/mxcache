package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.*;

/**
 * Project: Maxifier
 * Created by: Yakoushin Andrey
 * Date: 15.02.2010
 * Time: 13:40:10
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
 */
public class ObjectStorageImpl<T> implements ObjectStorage<T> {
    private volatile Object value = UNDEFINED;

    @Override
    public Object load() {
        return value;
    }

    @Override
    public void save(T v) {
        value = v;
    }

    @Override
    public void clear() {
        value = UNDEFINED;
    }

    @Override
    public int size() {
        return value == UNDEFINED ? 0 : 1;
    }
}