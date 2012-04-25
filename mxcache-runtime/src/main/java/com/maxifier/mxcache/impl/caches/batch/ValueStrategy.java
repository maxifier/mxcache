package com.maxifier.mxcache.impl.caches.batch;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 25.04.12
 * Time: 18:16
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
