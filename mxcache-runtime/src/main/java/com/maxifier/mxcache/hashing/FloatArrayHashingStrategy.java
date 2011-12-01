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
public final class FloatArrayHashingStrategy implements TObjectHashingStrategy<float[]> {
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