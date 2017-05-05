/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.storage.elementlocked;

import com.maxifier.mxcache.storage.*;

/**
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2OStorage.template
 *
 * <p>
 * This is a basic implementation of cache storage. It is very similar to a usual map.
 * It supports two main operations: load and save for corresponding key and value types.
 * </p>
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface ShortObjectElementLockedStorage extends ShortObjectStorage, ElementLockedStorage {
    /**
     * <p>This method should lock given cache key.</p>
     * <p>If this key is already locked the method should wait for corresponding unlock call.</p>
     * <p>This lock should be consistent with overall lock of this cache used for cleaning.</p>
     * <p>It is not necessary to have per-key granularity of locks. It is allowed for this method to lock a
     * group of keys at once, e.g. all with certain hash code of key or whatever else.</p>
     * @param key key
     */
    void lock(short key);

    /**
     * <p>Unlocks the cache key</p>
     * @param key cache key
     */
    void unlock(short key);
}
