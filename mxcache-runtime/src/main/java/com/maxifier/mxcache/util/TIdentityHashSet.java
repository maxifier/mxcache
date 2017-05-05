/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.util;

import gnu.trove.set.hash.TCustomHashSet;
import gnu.trove.strategy.IdentityHashingStrategy;

import java.util.Collection;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class TIdentityHashSet<T> extends TCustomHashSet<T> {
    public TIdentityHashSet() {
        super(IdentityHashingStrategy.INSTANCE);
    }

    public TIdentityHashSet(int initialCapacity) {
        super(IdentityHashingStrategy.INSTANCE, initialCapacity);
    }

    public TIdentityHashSet(int initialCapacity, float loadFactor) {
        super(IdentityHashingStrategy.INSTANCE, initialCapacity, loadFactor);
    }

    public TIdentityHashSet(Collection<? extends T> ts) {
        super(IdentityHashingStrategy.INSTANCE, ts);
    }
}
