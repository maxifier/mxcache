package com.maxifier.mxcache.hashing;

import gnu.trove.TObjectHashingStrategy;

import java.io.ObjectStreamException;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 01.07.2010
 * Time: 14:42:12
 */
public final class IntArrayHashingStrategy implements TObjectHashingStrategy<int[]> {
    private static final long serialVersionUID = 100L;

    private static final IntArrayHashingStrategy INSTANCE = new IntArrayHashingStrategy();

    private IntArrayHashingStrategy() {}

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