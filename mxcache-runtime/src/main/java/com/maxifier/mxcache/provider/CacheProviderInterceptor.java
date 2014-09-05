/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.provider;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.impl.RegistryEntry;

import javax.annotation.Nullable;

/**
 * <p>
 * CacheProviderInterceptor allows to override default behavior of CacheProvider.
 * </p><p>
 * To install interceptor use {@link com.maxifier.mxcache.MxCache#intercept}.
 * The use of interceptor may affect cache performance.
 * </p><p>
 * Interceptors form a chain. The later added interceptors will be called later.
 * Once interceptor is added you cannot undo it's effect though you can remove
 * an interceptor with {@link com.maxifier.mxcache.MxCache#removeInterceptor}}
 * and it will not affect all caches registered or created after its removal.
 * </p><p>
 * All runtime exceptions in interceptors are ignored.
 * </p>
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-09-02 10:55)
 */
public interface CacheProviderInterceptor {
    /**
     * This method allows to modify cache descriptor on cache registration.
     * The method takes an original cache descriptor as an argument.
     * The returned cache descriptor will be used instead of original one.
     *
     * If you don't need to override descriptor then just return null.
     * There's effectively no difference between returning null and original descriptor.
     *
     * @param descriptor cache descriptor
     * @param <T> cache owner type
     * @return original or overriding descriptor; null to override nothing
     */
    @Nullable
    <T> CacheDescriptor<T> registerCache(CacheDescriptor<T> descriptor);

    /**
     * This method allows to replace original cache created by MxCache.
     *
     * If you don't need to override cache then just return null.
     * There's effectively no difference between returning null and original cache.
     *
     * <b>You should never store direct references to Caches as this may lead to memory leak!</b>
     *
     * @param registryEntry registry entry associated with the descriptor
     * @param instance cache owner or null for static cache
     * @param context cache context
     * @param cache original cache created by mxcache
     * @return original or overriding cache; null to override nothing
     */
    @Nullable
    <T> Cache createCache(RegistryEntry<T> registryEntry, @Nullable T instance, CacheContext context, Cache cache);
}
