/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.batch;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
final class ListKeyStrategy<T> implements KeyStrategy<List<T>, T, List<T>> {
    private ListKeyStrategy() {}

    private static final ListKeyStrategy INSTANCE = new ListKeyStrategy();

    public static <T> ListKeyStrategy<T> getInstance() {
        //noinspection unchecked
        return INSTANCE;
    }

    @Override
    public int size(List<T> key) {
        return key.size();
    }

    @Override
    public List<T> iterator(List<T> ts) {
        return ts;
    }

    @Override
    public T get(int order, List<T> iterator) {
        return iterator.get(order);
    }

    @Override
    public List<T> toKey(List<T> ts, int count) {
        return ts;
    }

    @Override
    public boolean isStableOrder() {
        return true;
    }

    @Override
    public List<T> create(List<T> expectedType) {
        return new ArrayList<T>(expectedType.size());
    }

    @Override
    public boolean put(List<T> key, int index, T value) {
        return key.add(value);
    }
}
