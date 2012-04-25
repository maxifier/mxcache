package com.maxifier.mxcache.impl.caches.batch;

import gnu.trove.THashSet;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 25.04.12
 * Time: 18:16
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
