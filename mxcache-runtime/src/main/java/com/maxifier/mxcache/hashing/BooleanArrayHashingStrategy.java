/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.hashing;

import java.io.ObjectStreamException;
import java.util.Arrays;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class BooleanArrayHashingStrategy implements gnu.trove.strategy.HashingStrategy<boolean[]> {
    private static final long serialVersionUID = 100L;

    private static final BooleanArrayHashingStrategy INSTANCE = new BooleanArrayHashingStrategy();

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