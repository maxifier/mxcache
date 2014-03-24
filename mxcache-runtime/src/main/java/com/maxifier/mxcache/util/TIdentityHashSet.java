/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.util;

import gnu.trove.THashSet;
import gnu.trove.TObjectIdentityHashingStrategy;

import java.util.Collection;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
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
