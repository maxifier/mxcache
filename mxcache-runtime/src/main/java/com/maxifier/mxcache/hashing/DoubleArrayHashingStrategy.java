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
public class DoubleArrayHashingStrategy implements TObjectHashingStrategy<double[]> {
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