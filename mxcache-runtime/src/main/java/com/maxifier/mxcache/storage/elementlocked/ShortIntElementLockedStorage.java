/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.storage.elementlocked;

import com.maxifier.mxcache.storage.*;

/**
 * ShortIntElementLockedStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PStorage.template
 *
 * <p>
 * This is a basic implementation of cache storage. It is very similar to a usual map.
 * It supports two main operations: load and save for corresponding key and value types.
 * </p><p>
 * The content of the storage should not be altered internally between invocations to isCalculated and load.
 * </p>
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface ShortIntElementLockedStorage extends ShortIntStorage, ElementLockedStorage {
    /**
     * <p>This method is invoked only after corresponding call to isCalculated().</p>
     * <p>It is guaranteed that it would be called with corresponding lock held.</p>
     * <p>The behavior in case of element not present in cache is not defined.<p>
     */
    void lock(short key);

    /**
     * <p>This method should save a value to a cache</p>
     * <p>It is guaranteed that it would be called with corresponding lock held.</p>
     */
    void unlock(short key);
}