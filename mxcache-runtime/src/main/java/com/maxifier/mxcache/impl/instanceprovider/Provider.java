/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.instanceprovider;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface Provider<T> {
    T get();
}
