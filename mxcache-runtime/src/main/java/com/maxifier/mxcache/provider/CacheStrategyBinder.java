/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.provider;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface CacheStrategyBinder<T extends CachingStrategy> {
    void to(T instance);
}
