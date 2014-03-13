/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import gnu.trove.*;

import com.maxifier.mxcache.storage.*;

/**
 * CharacterByteTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PTroveStorage.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class CharacterByteTroveStorage extends TShortByteHashMap implements CharacterByteStorage {
    public CharacterByteTroveStorage() {
    }

    public CharacterByteTroveStorage(TShortHashingStrategy strategy) {
        super(strategy);
    }

    @Override
    public boolean isCalculated(char o) {
        return super.contains((short)o);
    }

    @Override
    public byte load(char o) {
        return super.get((short)o);
    }

    @Override
    public void save(char o, byte t) {
        put((short)o, t);
    }
}