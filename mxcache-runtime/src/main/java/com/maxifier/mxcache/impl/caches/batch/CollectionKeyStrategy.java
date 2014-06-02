/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.batch;

import gnu.trove.set.hash.THashSet;

import java.util.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
final class CollectionKeyStrategy<T> implements KeyStrategy<Collection<T>, T, Iterator<T>> {
    private CollectionKeyStrategy() {}

    private static final CollectionKeyStrategy INSTANCE = new CollectionKeyStrategy();

    public static <T> CollectionKeyStrategy<T> getInstance() {
        //noinspection unchecked
        return INSTANCE;
    }

    @Override
    public int size(Collection<T> key) {
        return key.size();
    }

    @Override
    public Iterator<T> iterator(Collection<T> key) {
        return key.iterator();
    }

    @Override
    public T get(int order, Iterator<T> iterator) {
        return iterator.next();
    }

    @Override
    public Collection<T> toKey(Collection<T> key, int n) {
        return key;
    }

    @Override
    public Collection<T> create(Collection<T> expectedType) {
        if (expectedType instanceof SortedSet) {
            return new TreeSet<T>();
        }
        if (expectedType instanceof Set) {
            return new THashSet<T>(expectedType.size());
        }
        return new ArrayList<T>(expectedType.size());
    }

    @Override
    public boolean isStableOrder() {
        return false;
    }

    @Override
    public boolean put(Collection<T> key, int index, T value) {
        return key.add(value);
    }
}
