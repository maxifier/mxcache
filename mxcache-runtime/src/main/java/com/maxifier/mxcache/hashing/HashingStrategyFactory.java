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
     * @param context контекст запроса
     * @param method метод
     * @return подходящую стратегию хэширования, или null, если достаточно стандартной
     */
    Object createHashingStrategy(CacheContext context, Method method);
}
