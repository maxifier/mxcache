package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.StatisticsModeEnum;
import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.impl.caches.def.*;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.interfaces.Statistics;
import com.maxifier.mxcache.interfaces.StatisticsHolder;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.Signature;
import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.impl.caches.storage.WrapperFactory;
import com.maxifier.mxcache.impl.caches.storage.Wrapping;
import com.maxifier.mxcache.provider.StorageFactory;
import com.maxifier.mxcache.storage.Storage;
import com.maxifier.mxcache.storage.elementlocked.ElementLockedStorage;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 09.09.2010
 * Time: 17:09:02
 */
public class StorageBasedCacheManager<T> extends AbstractCacheManager<T> {
    private final StorageFactory<T> storageFactory;

    private final Signature cacheSignature;

    private WrapperFactory wrapperFactoryCache;
    private boolean elementLockedCache;
    private Signature storageSignatureCache;
    private boolean inlineCache;

    public StorageBasedCacheManager(CacheContext context, CacheDescriptor<T> descriptor, StorageFactory<T> storageFactory) {
        super(context, descriptor);
        this.storageFactory = storageFactory;
        cacheSignature = descriptor.getSignature();
        // проверка идет по классу, а не instanceof потому что наследники могут переопределить поведение
        inlineCache = storageFactory.getClass() == DefaultStorageFactory.class && descriptor.getSignature().getContainer() == null;
    }

    @NotNull
    @Override
    protected Cache createCache(T owner, DependencyNode dependencyNode, MutableStatistics statistics) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        if (inlineCache) {
            return createInlineCache(owner, dependencyNode, statistics);
        }
        Storage storage = storageFactory.createStorage(owner);
        StatisticsModeEnum statisticsMode = getStatisticsMode();
        switch (statisticsMode) {
            case STORAGE:
                statistics = (MutableStatistics)((StatisticsHolder)storage).getStatistics();
                break;
            case STATIC_OR_STORAGE:
                if (storage instanceof StatisticsHolder) {
                    Statistics stat = ((StatisticsHolder) storage).getStatistics();
                    if (stat instanceof MutableStatistics) {
                        statistics = (MutableStatistics) stat;
                    }
                }
                break;
        }
        return getWrapperFactory(storage instanceof ElementLockedStorage, Signature.ofStorage(storage.getClass()))
                .wrap(owner, getDescriptor().getCalculable(), dependencyNode, storage, statistics);
    }

    private Cache createInlineCache(T owner, DependencyNode dependencyNode, MutableStatistics statistics) {
        assert inlineCache;
        CacheDescriptor<T> descriptor = getDescriptor();
        Class valueType = descriptor.getSignature().getValue();
        Object calculable = descriptor.getCalculable();
        if (valueType == boolean.class) {
            return new BooleanInlineCacheImpl(owner, (BooleanCalculatable) calculable, dependencyNode, statistics);
        }
        if (valueType == byte.class) {
            return new ByteInlineCacheImpl(owner, (ByteCalculatable) calculable, dependencyNode, statistics);
        }
        if (valueType == short.class) {
            return new ShortInlineCacheImpl(owner, (ShortCalculatable) calculable, dependencyNode, statistics);
        }
        if (valueType == char.class) {
            return new CharacterInlineCacheImpl(owner, (CharacterCalculatable) calculable, dependencyNode, statistics);
        }
        if (valueType == int.class) {
            return new IntInlineCacheImpl(owner, (IntCalculatable) calculable, dependencyNode, statistics);
        }
        if (valueType == long.class) {
            return new LongInlineCacheImpl(owner, (LongCalculatable) calculable, dependencyNode, statistics);
        }
        if (valueType == float.class) {
            return new FloatInlineCacheImpl(owner, (FloatCalculatable) calculable, dependencyNode, statistics);
        }
        if (valueType == double.class) {
            return new DoubleInlineCacheImpl(owner, (DoubleCalculatable) calculable, dependencyNode, statistics);
        }
        //noinspection unchecked
        return new ObjectInlineCacheImpl(owner, (ObjectCalculatable) calculable, dependencyNode, statistics);
    }


    private synchronized WrapperFactory getWrapperFactory(boolean elementLocked, Signature storageSignature) {
        if (wrapperFactoryCache == null || !storageSignature.equals(storageSignatureCache) || elementLocked != elementLockedCache) {
            wrapperFactoryCache = Wrapping.getFactory(storageSignature, cacheSignature, getDescriptor().getKeyTransform(), getDescriptor().getValueTransform(), elementLocked);
            storageSignatureCache = storageSignature;
            elementLockedCache = elementLocked;
        }
        return wrapperFactoryCache;
    }

    @Override
    public String getImplementationDetails() {
        return storageFactory.getImplementationDetails();
    }

}
