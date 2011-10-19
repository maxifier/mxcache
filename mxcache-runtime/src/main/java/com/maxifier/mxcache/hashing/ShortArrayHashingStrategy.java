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
public class ShortArrayHashingStrategy implements TObjectHashingStrategy<short[]> {
    private static final long serialVersionUID = 100L;

    private static final ShortArrayHashingStrategy INSTANCE = new ShortArrayHashingStrategy();

    private ShortArrayHashingStrategy() {}

    public static ShortArrayHashingStrategy getInstance() {
        return INSTANCE;
    }

    @Override
    public int computeHashCode(short[] object) {
        return Arrays.hashCode(object);
    }

    @Override
    public boolean equals(short[] o1, short[] o2) {
        return Arrays.equals(o1, o2);
    }

    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }
}