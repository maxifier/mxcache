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
public class CharacterLongTroveStorage extends gnu.trove.map.hash.TShortLongHashMap implements CharacterLongStorage {
    @Override
    public boolean isCalculated(char o) {
        return super.contains((short)o);
    }

    @Override
    public long load(char o) {
        return super.get((short)o);
    }

    @Override
    public void save(char o, long t) {
        put((short)o, t);
    }
}