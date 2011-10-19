package com.maxifier.mxcache.legacy;

import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.impl.StorageBasedCacheManager;
import com.maxifier.mxcache.legacy.converters.MxConvertType;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.proxy.WeakProxyFactory;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 01.07.2010
 * Time: 10:24:45
 */
public class PooledCacheManager<T, Value, ElementType extends MxConvertType> extends StorageBasedCacheManager<T> {
    public PooledCacheManager(CacheContext context, CacheDescriptor<T> descriptor, final ElementType elementType, final MxCachePoolManager<Value> cache, final MxPooledCacheStrategy<Value, ElementType> strategy) {
        super(context, descriptor.overrideProxyFactory(WeakProxyFactory.class), new PooledStorageFactory<T, Value, ElementType>(cache, elementType, strategy));
        if (descriptor.getKeyType() == null) {
            throw new IllegalArgumentException("Pooled cache needs key");
        }
        if (descriptor.getKeyType().isPrimitive() || descriptor.getValueType().isPrimitive()) {
            throw new IllegalArgumentException("Pooled cache requires key and value to be reference types");
        }
    }
}