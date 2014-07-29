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
public class CharacterBooleanTroveStorage extends gnu.trove.map.hash.TShortByteHashMap implements CharacterBooleanStorage {
    @Override
    public boolean isCalculated(char o) {
        return super.contains((short)o);
    }

    @Override
    public boolean load(char o) {
        return super.get((short)o) != 0;
    }

    @Override
    public void save(char o, boolean t) {
        put((short)o, (byte)(t? 1 : 0));
    }
}