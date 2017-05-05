/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.storage;

/**
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2OStorage.template
 *
 * <p>
 * This is a basic implementation of cache storage. It is very similar to a usual map.
 * It supports two main operations: load and save for corresponding key and value types.
 * </p>
 * <p>
 * There are no storages with primitive value types due to the following:
 * </p>
 * <ul>
 * <li>using object as value allows to store special marker-values (UNDEFINED, exceptions, etc.) without an overhead;
 * </li>
 * <li>it simplifies the code: you don't need to have a separate 'isCalculated(key)' method.</li>
 * </ul>
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface LongObjectStorage extends Storage {
    /**
     * <p>This method should extract value for given key from internal representation.</p>
     * <p>It is guaranteed that it would be called with corresponding lock held.</p>
     * @param key key
     * @return {@link Storage#UNDEFINED} if no value for key exists, value itself if it's set for given key.
     */
    Object load(long key);

    /**
     * <p>Saves a value to cache.</p>
     * <p>It is guaranteed that it would be called with corresponding lock held.</p>
     * @param key cache key
     * @param value cache value
     */
    void save(long key, Object value);
}
