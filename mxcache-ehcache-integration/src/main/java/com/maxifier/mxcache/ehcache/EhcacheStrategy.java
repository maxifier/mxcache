package com.maxifier.mxcache.ehcache;

import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.impl.StorageBasedCacheManager;
import com.maxifier.mxcache.provider.*;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 14.03.11
 * Time: 17:42
 */
public class EhcacheStrategy implements CachingStrategy {
    private final net.sf.ehcache.CacheManager cacheManager = new net.sf.ehcache.CacheManager();

    @NotNull
    @Override
    public <T> CacheManager<T> getManager(CacheContext context, final CacheDescriptor<T> descriptor) {
        if (descriptor.getKeyType() == null) {
            throw new IllegalArgumentException("Ehcache should use key");
        }
        return new StorageBasedCacheManager<T>(context, descriptor, new EhcacheStorageFactory<T>(descriptor, cacheManager));
    }

}
