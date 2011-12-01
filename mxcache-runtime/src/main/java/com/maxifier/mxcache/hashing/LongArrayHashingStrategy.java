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
public final class LongArrayHashingStrategy implements TObjectHashingStrategy<long[]> {
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