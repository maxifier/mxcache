package com.maxifier.mxcache.hashing;

import com.maxifier.mxcache.tuple.Tuple;
import gnu.trove.TObjectHashingStrategy;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 01.07.2010
 * Time: 14:14:35
 */
public final class TupleHashingStrategy implements TObjectHashingStrategy<Tuple> {
    private static final long serialVersionUID = 100L;

    /** Большое простое число, просто хэш для null */
    private static final int NULL_HASH = 30029;

    private final Object[] strategies;

    public TupleHashingStrategy(Object[] strategies) {
        this.strategies = strategies;
    }

    @Override
    public int computeHashCode(Tuple object) {
        return object == null ? NULL_HASH : object.hashCode(strategies);
    }

    @Override
    public boolean equals(Tuple o1, Tuple o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        return o1.equals(o2, strategies);
    }
}
