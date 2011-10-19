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
public class ByteArrayHashingStrategy implements TObjectHashingStrategy<byte[]> {
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