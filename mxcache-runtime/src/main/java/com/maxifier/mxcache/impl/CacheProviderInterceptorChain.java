/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.CacheProviderInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.List;

/**
 * CacheProviderInterceptorChain is an interceptor that includes other interceptors and allows to add and remove
 * new ones. Unlike other interceptors it never returns nulls.
 *
 * This chain ignores all runtime exceptions in interceptors
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-09-02 11:03)
 */
@ThreadSafe
public class CacheProviderInterceptorChain implements CacheProviderInterceptor {
    private final List<CacheProviderInterceptor> interceptors;
    private static final Logger logger = LoggerFactory.getLogger(CacheProviderImpl.class);

    CacheProviderInterceptorChain() {
        this.interceptors = new ArrayList<CacheProviderInterceptor>();
    }

    public synchronized void add(CacheProviderInterceptor interceptor) {
        if (interceptor == null) {
            throw new IllegalArgumentException("Null interceptor");
        }
        interceptors.add(interceptor);
    }

    public synchronized boolean remove(CacheProviderInterceptor interceptor) {
        return interceptors.remove(interceptor);
    }

    /**
     * Unlike other interceptors, the chain never returns null.
     */
    @Override
    @Nonnull
    public synchronized <T> CacheDescriptor<T> registerCache(CacheDescriptor<T> descriptor) {
        CacheDescriptor<T> res = descriptor;
        for (CacheProviderInterceptor interceptor : interceptors) {
            try {
                CacheDescriptor<T> override = interceptor.registerCache(res);
                if (override != null) {
                    res = override;
                }
            } catch (RuntimeException e) {
                logger.error("Exception in registerCache(" + descriptor + ") in interceptor " + interceptor, e);
            }
        }
        return res;
    }

    /**
     * Unlike other interceptors, the chain never returns null.
     */
    @Override
    @Nonnull
    public synchronized <T> Cache createCache(RegistryEntry<T> registryEntry,
                                          @Nullable T instance,
                                          CacheContext context,
                                          Cache cache) {
        Cache res = cache;
        for (CacheProviderInterceptor interceptor : interceptors) {
            try {
                Cache override = interceptor.createCache(registryEntry, instance, context, res);
                if (override != null) {
                    res = override;
                }
            } catch (RuntimeException e) {
                // do not add instance to log as this may cause another exception in toString()
                logger.error("Exception in createCache(" + registryEntry.getDescriptor(), " <instance>, " + context + ", " + cache + ") in interceptor " + interceptor, e);
            }
        }
        return res;
    }
}
