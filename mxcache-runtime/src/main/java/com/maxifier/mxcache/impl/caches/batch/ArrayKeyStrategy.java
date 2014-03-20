/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.batch;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
final class ArrayKeyStrategy<T> implements KeyStrategy<T[], T, T[]> {
    private ArrayKeyStrategy() {}

    private static final ArrayKeyStrategy INSTANCE = new ArrayKeyStrategy();

    public static <T> ArrayKeyStrategy<T> getInstance() {
        //noinspection unchecked
        return INSTANCE;
    }

    @Override
    public int size(T[] key) {
        return key.length;
    }

    @Override
    public T[] iterator(T[] key) {
        return key;
    }

    @Override
    public T get(int order, T[] iterator) {
        return iterator[order];
    }

    @Override
    public T[] toKey(T[] key, int count) {
        return count == key.length ? key : Arrays.copyOf(key, count);
    }

    @Override
    public T[] create(T[] expectedType) {
        //noinspection unchecked
        return (T[]) Array.newInstance(expectedType.getClass().getComponentType(), expectedType.length);
    }

    @Override
    public boolean isStableOrder() {
        return true;
    }

    @Override
    public boolean put(T[] key, int order, T value) {
        key[order] = value;
        return true;
    }
}
