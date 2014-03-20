/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.hashing;

import gnu.trove.TObjectHashingStrategy;

import java.io.ObjectStreamException;
import java.util.Arrays;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class ShortArrayHashingStrategy implements TObjectHashingStrategy<short[]> {
    private static final long serialVersionUID = 100L;

    private static final ShortArrayHashingStrategy INSTANCE = new ShortArrayHashingStrategy();

    private ShortArrayHashingStrategy() {}

    public static ShortArrayHashingStrategy getInstance() {
        return INSTANCE;
    }

    @Override
    public int computeHashCode(short[] object) {
        return Arrays.hashCode(object);
    }

    @Override
    public boolean equals(short[] o1, short[] o2) {
        return Arrays.equals(o1, o2);
    }

    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }
}