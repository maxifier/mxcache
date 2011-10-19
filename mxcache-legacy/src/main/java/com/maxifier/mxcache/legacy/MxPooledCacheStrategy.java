package com.maxifier.mxcache.legacy;

import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.legacy.converters.MxConvertType;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.CacheManager;
import com.maxifier.mxcache.provider.CachingStrategy;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 01.07.2010
 * Time: 9:56:18
 */
public abstract class MxPooledCacheStrategy<Value, ElementType extends MxConvertType> implements CachingStrategy {
    @NotNull
    @Override
    public <T> CacheManager<T> getManager(CacheContext context, CacheDescriptor<T> descriptor) {
        checkDescriptor(descriptor);
        return new PooledCacheManager<T, Value, ElementType>(context, descriptor, getElementType(descriptor), getCacheManager(descriptor), this);
    }

    protected abstract Confidence getConfidence(ElementType type);

    protected abstract ElementType getElementType(CacheDescriptor<?> descriptor);

    protected abstract <T> MxCachePoolManager<Value> getCacheManager(CacheDescriptor<T> descriptor);

    protected abstract <T> void checkDescriptor(CacheDescriptor<T> descriptor);
}
