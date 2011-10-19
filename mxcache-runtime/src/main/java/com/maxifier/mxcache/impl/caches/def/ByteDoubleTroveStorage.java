package com.maxifier.mxcache.impl.caches.def;

import gnu.trove.*;

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

public class ByteDoubleTroveStorage extends TByteDoubleHashMap implements ByteDoubleStorage {
    public ByteDoubleTroveStorage() {
    }

    public ByteDoubleTroveStorage(TByteHashingStrategy strategy) {
        super(strategy);
    }

    @Override
    public boolean isCalculated(byte o) {
        return super.contains(o);
    }

    @Override
    public double load(byte o) {
        return super.get(o);
    }

    @Override
    public void save(byte o, double t) {
        put(o, t);
    }
}