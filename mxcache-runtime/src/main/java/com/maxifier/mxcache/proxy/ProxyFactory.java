/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.proxy;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
public interface ProxyFactory<T> {
    T proxy(Class<T> expected, Resolvable<T> value);
}
