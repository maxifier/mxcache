/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.hashing;

import java.io.ObjectStreamException;
import java.util.Arrays;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class IntArrayHashingStrategy implements gnu.trove.strategy.HashingStrategy<int[]> {
    private static final long serialVersionUID = 100L;

    private static final IntArrayHashingStrategy INSTANCE = new IntArrayHashingStrategy();

    public static IntArrayHashingStrategy getInstance() {
        return INSTANCE;
    }

    @Override
    public int computeHashCode(int[] object) {
        return Arrays.hashCode(object);
    }

    @Override
    public boolean equals(int[] o1, int[] o2) {
        return Arrays.equals(o1, o2);
    }

    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }
}