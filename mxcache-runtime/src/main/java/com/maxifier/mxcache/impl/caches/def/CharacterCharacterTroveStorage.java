/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.*;

/**
 * CharacterCharacterTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PTroveStorage.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class CharacterCharacterTroveStorage extends gnu.trove.map.hash.TShortShortHashMap implements CharacterCharacterStorage {
    @Override
    public boolean isCalculated(char o) {
        return super.contains((short)o);
    }

    @Override
    public char load(char o) {
        return (char)super.get((short)o);
    }

    @Override
    public void save(char o, char t) {
        put((short)o, (short)t);
    }
}