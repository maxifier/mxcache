package com.maxifier.mxcache.ehcache;

import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.StorageFactory;
import com.maxifier.mxcache.storage.Storage;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import javax.annotation.Nonnull;

import java.lang.reflect.InvocationTargetException;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 14.03.11
* Time: 18:33
*/
public class EhcacheStorageFactory<T> implements StorageFactory<T> {
    private final CacheManager cacheManager;
    private final CacheDescriptor descriptor;

    public EhcacheStorageFactory(CacheDescriptor<T> descriptor, CacheManager cacheManager) {
        this.descriptor = descriptor;
        this.cacheManager = cacheManager;
    }

    @Nonnull
    @Override
    public Storage createStorage(T owner) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        String name = descriptor.getCacheName();
        Cache cache = cacheManager.getCache(name);
        return new EhcacheStorage(cache);
    }

    @Override
    public String getImplementationDetails() {
        return "EhCache";
    }
}
