/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.batch;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
interface ValueStrategy<KE, V, VE, C> {
    void addUnknown(V knownValues, C composer, int i, KE k);

    void addKnown(V knownValues, C composer, int i, KE k, VE v);

    V createValue(Class<V> valueType, int n);

    C createComposer(int n);

    V compose(V knownValue, V calculated, C composition);

    VE get(V value, int index, KE key);

    boolean requiresOrder();
}
