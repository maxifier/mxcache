/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.batch;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
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
