/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.hashing;

import com.maxifier.mxcache.context.CacheContext;

import java.lang.reflect.Method;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface HashingStrategyFactory {
    /**
     * @param context cache context
     * @param method method invoked
     * @return creates a suitable hashing strategy of null if standard one should be used
     */
    Object createHashingStrategy(CacheContext context, Method method);
}
