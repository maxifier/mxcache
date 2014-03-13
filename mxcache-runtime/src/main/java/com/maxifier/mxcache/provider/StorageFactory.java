/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.provider;

import com.maxifier.mxcache.storage.Storage;

import javax.annotation.Nonnull;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface StorageFactory<T> {
    @Nonnull
    Storage createStorage(T owner) throws InvocationTargetException, IllegalAccessException, InstantiationException;

    String getImplementationDetails();
}
