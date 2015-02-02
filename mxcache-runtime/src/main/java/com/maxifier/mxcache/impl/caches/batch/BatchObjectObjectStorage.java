/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.batch;

import com.maxifier.mxcache.caches.Calculable;
import com.maxifier.mxcache.caches.ObjectObjectCalculatable;
import com.maxifier.mxcache.storage.CalculableInterceptor;
import com.maxifier.mxcache.storage.ObjectObjectStorage;
import javax.annotation.Nullable;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class BatchObjectObjectStorage<K, V, C, KeyElement, ValueElement, KeyIterator>
        implements ObjectObjectStorage<K>, CalculableInterceptor, ObjectObjectCalculatable<K, V> {

    private final ObjectObjectStorage<KeyElement> storage;

    private final KeyStrategy<K, KeyElement, KeyIterator> keyStrategy;
    private final ValueStrategy<KeyElement, V, ValueElement, C> valueStrategy;

    private final Class<V> valueType;

    private ObjectObjectCalculatable<K, V> realCalculable;

    private C composition;
    private K unknownKey;
    private V knownValue;

    protected BatchObjectObjectStorage(ObjectObjectStorage<KeyElement> storage,
                                       KeyStrategy<K, KeyElement, KeyIterator> keyStrategy,
                                       ValueStrategy<KeyElement, V, ValueElement, C> valueStrategy,

                                       Class<V> valueType) {
        this.storage = storage;
        this.keyStrategy = keyStrategy;
        this.valueStrategy = valueStrategy;
        this.valueType = valueType;
    }

    @Override
    public Object load(K key) {
        decompose(key);
        if (unknownKey == null) {
            V res = knownValue;
            reset();
            return res;
        }
        return UNDEFINED;
    }
    
    protected Object loadElement(KeyElement element) {
        return storage.load(element);
    }

    @Override
    public Calculable createInterceptedCalculable(Calculable calculable) {
        //noinspection unchecked
        this.realCalculable = (ObjectObjectCalculatable<K, V>) calculable;
        return this;
    }

    @Override
    public void save(K key, Object value) {
        reset();
    }

    void set(V result, K unknownKey, @Nullable C composition) {
        this.knownValue = result;
        this.unknownKey = unknownKey;
        this.composition = composition;
    }

    void reset() {
        knownValue = null;
        unknownKey = null;
        composition = null;
    }

    protected void decompose(K key) {
        KeyStrategy<K, KeyElement, KeyIterator> keyStrategy = this.keyStrategy;
        ValueStrategy<KeyElement, V, ValueElement, C> valueStrategy = this.valueStrategy;

        K unknownKey = keyStrategy.create(key);
        int n = keyStrategy.size(key);
        V knownValues = valueStrategy.createValue(valueType, n);
        C composer = valueStrategy.createComposer(n);

        int unknownCount = 0;

        KeyIterator it = keyStrategy.iterator(key);
        for (int i = 0; i<n; i++) {
            KeyElement k = keyStrategy.get(i, it);
            Object v = loadElement(k);
            if (v == UNDEFINED) {
                valueStrategy.addUnknown(knownValues, composer, i, k);
                if (keyStrategy.put(unknownKey, unknownCount, k)) {
                    unknownCount++;
                }
            } else {
                //noinspection unchecked
                valueStrategy.addKnown(knownValues, composer, i, k, (ValueElement) v);
            }
        }
        //noinspection unchecked
        set(knownValues, unknownCount == 0 ? null : keyStrategy.toKey(unknownKey, unknownCount), composer);
    }

    private void saveReal(K unknownKey, V calculated) {
        int n = keyStrategy.size(unknownKey);
        KeyIterator it = keyStrategy.iterator(unknownKey);
        for (int i = 0; i<n; i++) {
            KeyElement k = keyStrategy.get(i, it);
            storage.save(k, valueStrategy.get(calculated, i, k));
        }
    }

    @Override
    public void clear() {
        storage.clear();
    }

    @Override
    public int size() {
        return storage.size();
    }

    @Override
    public V calculate(Object owner, K key) {
        boolean success = false;
        try {
            K unknownKey = this.unknownKey;
            V calculated = realCalculable.calculate(owner, unknownKey);

            saveReal(unknownKey, calculated);
            V res = valueStrategy.compose(knownValue, calculated, composition);
            // there are many cases when exception can be thrown, e.g. OOM on valueStrategy.compose
            // we need to reset current value in all cases when the methods hasn't succeeded.
            success = true;
            return res;
        } finally {
            if (!success) {
                // to prevent memory leak if exception is thrown
                reset();
            }
        }
    }
}
