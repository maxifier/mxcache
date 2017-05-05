/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.hashing;

import java.io.ObjectStreamException;
import java.util.Arrays;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class ArrayHashingStrategy<T> implements gnu.trove.strategy.HashingStrategy<T[]> {
    private static final long serialVersionUID = 100L;

    private static final ArrayHashingStrategy INSTANCE = new ArrayHashingStrategy();

    @SuppressWarnings({"unchecked"})
    public static <T> ArrayHashingStrategy<T> getInstance() {
        return INSTANCE;
    }

    @Override
    public int computeHashCode(T[] object) {
        return Arrays.deepHashCode(object);
    }

    @Override
    public boolean equals(T[] o1, T[] o2) {
        return Arrays.deepEquals(o1, o2);
    }

    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }
}
