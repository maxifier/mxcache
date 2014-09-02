/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.interceptor;

import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.MxCache;
import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.caches.IntIntCache;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.interfaces.Statistics;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.CacheProviderInterceptor;
import org.testng.annotations.Test;

import javax.annotation.Nullable;
import java.util.concurrent.locks.Lock;

import static org.testng.Assert.assertEquals;

/**
 * TestInterceptor
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-09-02 11:14)
 */
public class CacheProviderInterceptorUTest {
    static class X {
        @Cached
        int intercepted(int v) {
            return v + 2;
        }

        @Cached
        int plus3(int v) {
            return v + 3;
        }

        @Cached
        int plus4(int v) {
            return v + 4;
        }
    }

    @Test
    void testOverrideCache() {
        TestInterceptor interceptor = new TestInterceptor();
        MxCache.intercept(interceptor);
        try {
            X x = new X();
            // original method would return 4 + 2 = 6, but it is intercepted with fake cache that returns 4 + 5 = 9
            assertEquals(x.intercepted(4), 9);

            // other methods are not affected
            assertEquals(x.plus3(4), 7);
            assertEquals(x.plus4(4), 8);
        } finally {
            MxCache.removeInterceptor(interceptor);
        }
    }

    static class TestInterceptor implements CacheProviderInterceptor {
        @Nullable
        @Override
        public <T> CacheDescriptor<T> registerCache(CacheDescriptor<T> descriptor) {
            return null;
        }

        @Nullable
        @Override
        public Cache createCache(CacheDescriptor<?> descriptor, final @Nullable Object instance, CacheContext context, Cache cache) {
            if (descriptor.getMethod().getName().equals("intercepted")) {
                return new IntIntCache() {
                    @Override
                    public int getOrCreate(int o) {
                        return o + 5;
                    }

                    @Override
                    public int getSize() {
                        return 0;
                    }

                    @Override
                    public CacheDescriptor getDescriptor() {
                        return null;
                    }

                    @Override
                    public void setDependencyNode(DependencyNode node) {

                    }

                    @Nullable
                    @Override
                    public Lock getLock() {
                        return null;
                    }

                    @Override
                    public void clear() {

                    }

                    @Override
                    public DependencyNode getDependencyNode() {
                        return null;
                    }

                    @Nullable
                    @Override
                    public Object getCacheOwner() {
                        return instance;
                    }

                    @Nullable
                    @Override
                    public Statistics getStatistics() {
                        return null;
                    }
                };
            }
            // otherwise no interception
            if (descriptor.getMethod().getName().equals("plus3")) {
                // two variants to not override:
                // return original cache...
                return cache;
            }
            // ...or return null
            return null;
        }
    }
}
