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
public class BooleanStorageImpl implements BooleanStorage {
    private volatile boolean set;
    private boolean value;

    @Override
    public boolean isCalculated() {
        return set;
    }

    @Override
    public boolean load() {
        return value; 
    }

    @Override
    public void save(boolean v) {
        set = true;
        value = v;
    }

    @Override
    public void clear() {
        set = false;
    }

    @Override
    public int size() {
        return set ? 1 : 0;
    }
}
