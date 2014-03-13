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
public final class ByteArrayHashingStrategy implements TObjectHashingStrategy<byte[]> {
    private static final long serialVersionUID = 100L;

    private static final ByteArrayHashingStrategy INSTANCE = new ByteArrayHashingStrategy();

    private ByteArrayHashingStrategy() {}

    public static ByteArrayHashingStrategy getInstance() {
        return INSTANCE;
    }

    @Override
    public int computeHashCode(byte[] object) {
        return Arrays.hashCode(object);
    }

    @Override
    public boolean equals(byte[] o1, byte[] o2) {
        return Arrays.equals(o1, o2);
    }

    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }
}