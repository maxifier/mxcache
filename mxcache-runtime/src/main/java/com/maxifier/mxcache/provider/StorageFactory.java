/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.provider;

import com.maxifier.mxcache.storage.Storage;

import javax.annotation.Nonnull;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface StorageFactory {
    /**
     * @param owner an owner of the cache. <b>Do not store direct references to owners outside of storage!</b>
     * @return an instance of cache storage for given owner. Each invocation should return a new instance of storage.
     * @throws Exception you are allowed to throw any exception. Exceptions are ignored, if storage factory fails a
     *    default one would be used.
     *
     */
    @Nonnull
    Storage createStorage(Object owner) throws Exception;

    /**
     * @return any string that represents your cache implementation details.
     *     This string is visible in MBean details.
     */
    String getImplementationDetails();
}
