package com.maxifier.mxcache.provider;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.context.CacheContext;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 22.04.2010
 * Time: 18:03:44
 */
public interface CacheManager<T> {
    /**
     * @return дескриптор, переданный при создании
     */
    CacheDescriptor<T> getDescriptor();

    /**
     * Создает экземпляр кэша
     * @param owner вдаделец кэша
     * @return экземпляр кэша
     */
    Cache createCache(@Nullable T owner);

    /**
     * @return implementation details, e.g. class name of storage/cache
     */
    String getImplementationDetails();

    CacheContext getContext();
}
