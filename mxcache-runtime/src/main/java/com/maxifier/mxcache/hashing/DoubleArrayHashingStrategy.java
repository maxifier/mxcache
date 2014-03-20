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
public final class DoubleArrayHashingStrategy implements TObjectHashingStrategy<double[]> {
    private static final long serialVersionUID = 100L;

    private static final DoubleArrayHashingStrategy INSTANCE = new DoubleArrayHashingStrategy();

    private DoubleArrayHashingStrategy() {}

    public static DoubleArrayHashingStrategy getInstance() {
        return INSTANCE;
    }

    @Override
    public int computeHashCode(double[] object) {
        return Arrays.hashCode(object);
    }

    @Override
    public boolean equals(double[] o1, double[] o2) {
        return Arrays.equals(o1, o2);
    }

    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }
}