package com.maxifier.mxcache.impl.caches.batch;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 25.04.12
 * Time: 18:16
 */
interface KeyStrategy<K, E, I> {
    int size(K key);

    I iterator(K k);

    E get(int order, I iterator);

    K toKey(K list, int count);

    boolean isStableOrder();
    
    boolean put(K key, int index, E value);

    K create(K expectedType);
}
