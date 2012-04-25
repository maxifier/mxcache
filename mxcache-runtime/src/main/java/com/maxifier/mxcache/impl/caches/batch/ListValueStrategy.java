package com.maxifier.mxcache.impl.caches.batch;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 25.04.12
 * Time: 14:38
 */
final class ListValueStrategy<K, V> implements ValueStrategy<K, List<V>, V, BitSet> {
    private ListValueStrategy() {}

    private static final ListValueStrategy INSTANCE = new ListValueStrategy();

    public static <K, V> ListValueStrategy<K, V> getInstance() {
        //noinspection unchecked
        return INSTANCE;
    }
    
    @Override
    public List<V> compose(List<V> knownValue, List<V> calculated, BitSet composition) {
        for (int i = composition.nextSetBit(0), p = 0; i >= 0; i = composition.nextSetBit(i+1), p++) {
            knownValue.set(i, calculated.get(p));
        }
        return knownValue;
    }

    @Override
    public  List<V> createValue(Class<List<V>> valueType, int n) {
        return new ArrayList<V>(n);
    }

    @Override
    public  BitSet createComposer(int n) {
        return new BitSet(n);
    }

    @Override
    public  void addUnknown(List<V> knownValues, BitSet composer, int i, K k) {
        composer.set(i);
        //noinspection unchecked
        knownValues.add(null);
    }

    @Override
    public void addKnown(List<V> knownValues, BitSet composer, int i, K k, V v) {
        knownValues.add(v);
    }

    @Override
    public  V get(List<V> value, int index, K key) {
        return value.get(index);
    }

    @Override
    public boolean requiresOrder() {
        return true;
    }
}
