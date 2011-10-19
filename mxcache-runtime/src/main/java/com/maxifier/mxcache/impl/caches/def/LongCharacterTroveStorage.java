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

public class LongCharacterTroveStorage extends TLongShortHashMap implements LongCharacterStorage {
    public LongCharacterTroveStorage() {
    }

    public LongCharacterTroveStorage(TLongHashingStrategy strategy) {
        super(strategy);
    }

    @Override
    public boolean isCalculated(long o) {
        return super.contains(o);
    }

    @Override
    public char load(long o) {
        return (char)super.get(o);
    }

    @Override
    public void save(long o, char t) {
        put(o, (short)t);
    }
}