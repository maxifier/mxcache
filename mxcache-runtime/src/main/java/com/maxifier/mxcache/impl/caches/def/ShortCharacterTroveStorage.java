/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.*;

/**
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PTroveStorage.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ShortCharacterTroveStorage extends gnu.trove.map.hash.TShortShortHashMap implements ShortCharacterStorage {
    @Override
    public boolean isCalculated(short o) {
        return super.contains(o);
    }

    @Override
    public char load(short o) {
        return (char)super.get(o);
    }

    @Override
    public void save(short o, char t) {
        put(o, (short)t);
    }
}