package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.impl.NullCacheManager;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.CacheManager;
import com.maxifier.mxcache.provider.CachingStrategy;

import javax.annotation.Nonnull;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 09.03.11
 * Time: 18:14
 */
public class TestStrategy implements CachingStrategy {
    @Nonnull
    @Override
    public <T> CacheManager<T> getManager(CacheContext context, CacheDescriptor<T> descriptor) {
        return new NullCacheManager<T>(descriptor);
    }
}
