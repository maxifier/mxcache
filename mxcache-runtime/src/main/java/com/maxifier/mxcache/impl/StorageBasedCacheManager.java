package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.StatisticsModeEnum;
import com.maxifier.mxcache.context.CacheContext;
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

    public StorageBasedCacheManager(CacheContext context, CacheDescriptor<T> descriptor, StorageFactory<T> storageFactory) {
        super(context, descriptor);
        this.storageFactory = storageFactory;
        cacheSignature = descriptor.getSignature();
    }

    @NotNull
    @Override
    protected Cache createCache(T owner, DependencyNode dependencyNode, MutableStatistics statistics) throws InstantiationException, IllegalAccessException, InvocationTargetException {
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
        return getWrapperFactory(storage instanceof ElementLockedStorage, Signature.of(storage.getClass()))
                .wrap(owner, getDescriptor().getCalculable(), dependencyNode, storage, statistics);
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
