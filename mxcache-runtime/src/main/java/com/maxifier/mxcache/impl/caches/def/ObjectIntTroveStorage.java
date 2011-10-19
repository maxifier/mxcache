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

public class ObjectIntTroveStorage<E> extends TObjectIntHashMap<E> implements ObjectIntStorage<E> {
    public ObjectIntTroveStorage() {
    }

    public ObjectIntTroveStorage(TObjectHashingStrategy<E> strategy) {
        super(strategy);
    }

    @Override
    public boolean isCalculated(E o) {
        return super.contains(o);
    }

    @Override
    public int load(E o) {
        return super.get(o);
    }

    @Override
    public void save(E o, int t) {
        put(o, t);
    }
}