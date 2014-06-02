/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.hashing;

import java.io.ObjectStreamException;
import java.util.Arrays;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class LongArrayHashingStrategy implements gnu.trove.strategy.HashingStrategy<long[]> {
    private static final long serialVersionUID = 100L;

    private static final LongArrayHashingStrategy INSTANCE = new LongArrayHashingStrategy();

    private LongArrayHashingStrategy() {}

    public static LongArrayHashingStrategy getInstance() {
        return INSTANCE;
    }

    @Override
    public int computeHashCode(long[] object) {
        return Arrays.hashCode(object);
    }

    @Override
    public boolean equals(long[] o1, long[] o2) {
        return Arrays.equals(o1, o2);
    }

    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }
}