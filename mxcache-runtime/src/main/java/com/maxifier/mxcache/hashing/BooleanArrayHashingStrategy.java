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
public class BooleanArrayHashingStrategy implements TObjectHashingStrategy<boolean[]> {
    private static final long serialVersionUID = 100L;

    private static final BooleanArrayHashingStrategy INSTANCE = new BooleanArrayHashingStrategy();

    private BooleanArrayHashingStrategy() {}

    public static BooleanArrayHashingStrategy getInstance() {
        return INSTANCE;
    }

    @Override
    public int computeHashCode(boolean[] object) {
        return Arrays.hashCode(object);
    }

    @Override
    public boolean equals(boolean[] o1, boolean[] o2) {
        return Arrays.equals(o1, o2);
    }

    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }
}