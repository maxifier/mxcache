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
public class CharArrayHashingStrategy implements TObjectHashingStrategy<char[]> {
    private static final long serialVersionUID = 100L;

    private static final CharArrayHashingStrategy INSTANCE = new CharArrayHashingStrategy();

    private CharArrayHashingStrategy() {}

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