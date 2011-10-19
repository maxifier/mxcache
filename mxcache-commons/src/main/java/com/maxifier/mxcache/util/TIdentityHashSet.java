package com.maxifier.mxcache.util;

import gnu.trove.THashSet;
import gnu.trove.TObjectIdentityHashingStrategy;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 01.05.2010
 * Time: 9:56:15
 */
@SuppressWarnings ({ "unchecked" })
public class TIdentityHashSet<T> extends THashSet<T> {
    private static final TObjectIdentityHashingStrategy STRATEGY = new TObjectIdentityHashingStrategy();

    public TIdentityHashSet() {
        super(STRATEGY);
    }

    public TIdentityHashSet(int initialCapacity) {
        super(initialCapacity, STRATEGY);
    }

    public TIdentityHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor, STRATEGY);
    }

    public TIdentityHashSet(Collection<? extends T> ts) {
        super(ts, STRATEGY);
    }
}
