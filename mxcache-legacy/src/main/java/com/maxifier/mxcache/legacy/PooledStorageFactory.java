package com.maxifier.mxcache.legacy;

import com.maxifier.mxcache.provider.StorageFactory;
import com.maxifier.mxcache.storage.Storage;
import com.maxifier.mxcache.legacy.converters.MxConvertType;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 15.11.2010
 * Time: 17:58:58
 */
class PooledStorageFactory<T, Value, ElementType extends MxConvertType> implements StorageFactory<T> {
    private final MxCachePoolManager<Value> cache;
    private final ElementType elementType;
    private final MxPooledCacheStrategy<Value, ElementType> strategy;

    public PooledStorageFactory(MxCachePoolManager<Value> cache, ElementType elementType, MxPooledCacheStrategy<Value, ElementType> strategy) {
        this.cache = cache;
        this.elementType = elementType;
        this.strategy = strategy;
    }

    @Override
    public Storage createStorage(Object owner) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        return new PooledCache<Object, Value, ElementType>(owner, cache, elementType, strategy);
    }

    @Override
    public String getImplementationDetails() {
        return "Pooled{cache=" + cache + ", elementType=" + elementType + ", strategy=" + strategy + '}';
    }

    @Override
    public String toString() {
        return getImplementationDetails();
    }
}
