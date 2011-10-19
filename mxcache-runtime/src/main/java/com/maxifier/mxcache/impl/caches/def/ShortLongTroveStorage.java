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

public class ShortLongTroveStorage extends TShortLongHashMap implements ShortLongStorage {
    public ShortLongTroveStorage() {
    }

    public ShortLongTroveStorage(TShortHashingStrategy strategy) {
        super(strategy);
    }

    @Override
    public boolean isCalculated(short o) {
        return super.contains(o);
    }

    @Override
    public long load(short o) {
        return super.get(o);
    }

    @Override
    public void save(short o, long t) {
        put(o, t);
    }
}