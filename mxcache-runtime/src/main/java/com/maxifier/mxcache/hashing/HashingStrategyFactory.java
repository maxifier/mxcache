package com.maxifier.mxcache.hashing;

import com.maxifier.mxcache.context.CacheContext;

import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 01.07.2010
 * Time: 15:01:18
 */
public interface HashingStrategyFactory {
    /**
     * @param context контекст запроса
     * @param method метод
     * @return подходящую стратегию хэширования, или null, если достаточно стандартной
     */
    Object createHashingStrategy(CacheContext context, Method method);
}
