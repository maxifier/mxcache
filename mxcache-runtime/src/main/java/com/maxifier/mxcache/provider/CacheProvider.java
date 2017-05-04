/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.provider;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.caches.Calculable;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.impl.CacheId;

import javax.annotation.Nonnull;

import javax.annotation.Nullable;

import java.util.List;

/**
 * <p>
 * Cache provider is a single access point for creation of cache instances.
 * </p><p>
 * It doesn't create caches by itself but it maintains a list of CacheManagers and delegates all calls to them.
 * </p>
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface CacheProvider {
    /**
     * All instrumented classes do invoke this method for every cached method (both static and instance).
     *
     * @param cacheOwner cache that defines cache method.
     * @param cacheId cache id, unique in the class, starting with 0.
     * @param keyType key type, null if a method has no arguments. If there are few arguments, key type will be a
     *                subclass of Tuple with corresponding key types.
     * @param valueType method return type
     * @param group group name from @Cached annotation
     * @param tags tag names from @Cached annotation
     * @param calculable the object that can invoke original method code
     * @param methodName method name
     * @param methodDesc method descriptor
     * @param cacheName name of cache, may be null (used by some strategies)
     */
    void registerCache(Class<?> cacheOwner, int cacheId, Class keyType, Class valueType, String group, String[] tags, Calculable calculable, String methodName, String methodDesc, @Nullable String cacheName);

    /**
     * This method is invoked from MxCache-generated code.
     * For static methods it is invoked from static initialization section, for non-static - from constructor.
     *
     * @param cacheOwner cache that defines cache method.
     * @param cacheId cache id, unique in the class, starting with 0.
     * @param instance instance of class that holds cache. null for static caches. This instance is guaranteed to be
     *                 a subclass of cacheOwner.
     * @param context cache context
     * @return cache instance
     */
    Cache createCache(@Nonnull Class cacheOwner, int cacheId, @Nullable Object instance, CacheContext context);

    CacheDescriptor getDescriptor(CacheId id);

    /**
     * @return return all cache managers that create caches.
     */
    List<CacheManager> getCaches();

    /**
     * Adds cache interceptor to interceptor chain.
     * @param interceptor the interceptor to add.
     */
    void intercept(CacheProviderInterceptor interceptor);

    /**
     * Removes given interceptor from the chain. Note: this doesn't undo the modifications
     * that this interceptor made during its life.
     *
     * @param interceptor the interceptor to remove
     * @return true if the interceptor was registered
     */
    boolean removeInterceptor(CacheProviderInterceptor interceptor);
}
