/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import gnu.trove.*;

import com.maxifier.mxcache.storage.*;
import static com.maxifier.mxcache.impl.caches.def.TroveHelper.*;

/**
 * CharacterObjectTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM #SOURCE#
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class CharacterObjectTroveStorage<T> extends TShortObjectHashMap<Object> implements CharacterObjectStorage<T> {
    public CharacterObjectTroveStorage() {
    }

    public CharacterObjectTroveStorage(TShortHashingStrategy strategy) {
        super(strategy);        
    }

    @Override
    public Object load(char key) {
        Object v = get((short)key);
        if (v == null) {
            return UNDEFINED;
        }
        if (v == NULL_REPLACEMENT) {
            return null;
        }
        return v;
    }

    @Override
    public void save(char key, T value) {
        put((short)key, value == null ? NULL_REPLACEMENT : value);
    }
}
