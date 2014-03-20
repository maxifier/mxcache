/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.batch;

import java.lang.reflect.Array;
import java.util.BitSet;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
final class ArrayValueStrategy<K, V> implements ValueStrategy<K, V[], V, BitSet> {
    private ArrayValueStrategy() {}

    private static final ArrayValueStrategy INSTANCE = new ArrayValueStrategy();

    public static <K, V> ArrayValueStrategy<K, V> getInstance() {
        //noinspection unchecked
        return INSTANCE;
    }

    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    @Override
    public V[] compose(V[] knownValue, V[] calculated, BitSet composition) {
        for (int i = composition.nextSetBit(0), p = 0; i >= 0; i = composition.nextSetBit(i+1), p++) {
            knownValue[i] = calculated[p];
        }
        return knownValue;
    }

    @Override
    public V[] createValue(Class<V[]> valueType, int n) {
        //noinspection unchecked
        return (V[]) Array.newInstance(valueType.getComponentType(), n);
    }

    @Override
    public BitSet createComposer(int n) {
        return new BitSet(n);
    }

    @Override
    public void addUnknown(V[] knownValues, BitSet composer, int i, K k) {
        composer.set(i);
    }

    @Override
    public void addKnown(V[] knownValues, BitSet composer, int i, K k, V v) {
        knownValues[i] = v;
    }

    @Override
    public V get(V[] value, int index, K key) {
        return value[index];
    }

    @Override
    public boolean requiresOrder() {
        return true;
    }
}
