/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.hashing;

import java.io.ObjectStreamException;
import java.util.Arrays;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class CharArrayHashingStrategy implements gnu.trove.strategy.HashingStrategy<char[]> {
    private static final long serialVersionUID = 100L;

    private static final CharArrayHashingStrategy INSTANCE = new CharArrayHashingStrategy();

    public static CharArrayHashingStrategy getInstance() {
        return INSTANCE;
    }

    @Override
    public int computeHashCode(char[] object) {
        return Arrays.hashCode(object);
    }

    @Override
    public boolean equals(char[] o1, char[] o2) {
        return Arrays.equals(o1, o2);
    }

    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }
}