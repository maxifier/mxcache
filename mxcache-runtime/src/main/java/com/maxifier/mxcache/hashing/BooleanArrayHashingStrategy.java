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
public final class BooleanArrayHashingStrategy implements TObjectHashingStrategy<boolean[]> {
    private static final long serialVersionUID = 100L;

    private static final BooleanArrayHashingStrategy INSTANCE = new BooleanArrayHashingStrategy();

    private BooleanArrayHashingStrategy() {}

    public static BooleanArrayHashingStrategy getInstance() {
        return INSTANCE;
    }

    @Override
    public int computeHashCode(boolean[] object) {
        return Arrays.hashCode(object);
    }

    @Override
    public boolean equals(boolean[] o1, boolean[] o2) {
        return Arrays.equals(o1, o2);
    }

    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }
}