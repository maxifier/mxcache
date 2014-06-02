/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.hashing;

import java.io.ObjectStreamException;
import java.util.Arrays;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class FloatArrayHashingStrategy implements gnu.trove.strategy.HashingStrategy<float[]> {
    private static final long serialVersionUID = 100L;

    private static final FloatArrayHashingStrategy INSTANCE = new FloatArrayHashingStrategy();

    private FloatArrayHashingStrategy() {}

    public static FloatArrayHashingStrategy getInstance() {
        return INSTANCE;
    }

    @Override
    public int computeHashCode(float[] object) {
        return Arrays.hashCode(object);
    }

    @Override
    public boolean equals(float[] o1, float[] o2) {
        return Arrays.equals(o1, o2);
    }

    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }
}