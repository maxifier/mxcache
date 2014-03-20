/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.StatisticsModeEnum;
import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.impl.caches.def.*;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.impl.resource.DependencyNodeVisitor;
import com.maxifier.mxcache.interfaces.Statistics;
import com.maxifier.mxcache.interfaces.StatisticsHolder;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.Signature;
import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.impl.wrapping.WrapperFactory;
import com.maxifier.mxcache.impl.wrapping.Wrapping;
import com.maxifier.mxcache.provider.StorageFactory;
import com.maxifier.mxcache.storage.CalculableInterceptor;
import com.maxifier.mxcache.storage.Storage;
import com.maxifier.mxcache.storage.elementlocked.ElementLockedStorage;
import com.maxifier.mxcache.util.TIdentityHashSet;

import javax.annotation.Nonnull;

import java.lang.ref.Reference;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class StorageBasedCacheManager<T> extends AbstractCacheManager<T> {
    public static final DependencyNode MARKER_NODE = new MarkerDependencyNode();

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
        inlineCache = canInlineCache(descriptor, storageFactory);
    }

    private boolean canInlineCache(CacheDescriptor<T> descriptor, StorageFactory<T> storageFactory) {
        return storageFactory.getClass() == DefaultStorageFactory.class &&
                descriptor.getSignature().getContainer() == null &&
                !descriptor.isResourceView();
    }

    @Nonnull
    @Override
    protected Cache createCache(T owner, DependencyNode dependencyNode, MutableStatistics statistics) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        if (inlineCache) {
            if (dependencyNode == MARKER_NODE) {
                return createInlineCacheWithDependencyNode(owner, statistics);
            }
            Cache cache = createInlineCache(owner, statistics);
            cache.setDependencyNode(dependencyNode);
            return cache;
        }
        Storage storage = storageFactory.createStorage(owner);
        StatisticsModeEnum statisticsMode = getStatisticsMode();
        switch (statisticsMode) {
            case STORAGE:
                statistics = (MutableStatistics)((StatisticsHolder)storage).getStatistics();
                break;
            case STATIC_OR_STORAGE:
                if (storage instanceof StatisticsHolder) {
                    Statistics storageStatistics = ((StatisticsHolder) storage).getStatistics();
                    if (storageStatistics instanceof MutableStatistics) {
                        statistics = (MutableStatistics) storageStatistics;
                    }
                }
                break;
        }
        Calculable calculable = getDescriptor().getCalculable();
        if (storage instanceof CalculableInterceptor) {
            calculable = ((CalculableInterceptor)storage).createInterceptedCalculable(calculable);
        }
        Cache cache = getWrapperFactory(storage instanceof ElementLockedStorage, Signature.ofStorage(storage.getClass()))
                .wrap(owner, calculable, storage, statistics);
        cache.setDependencyNode(dependencyNode);
        return cache;
    }

    @Override
    protected DependencyNode createInstanceNode() {
        if (inlineCache) {
            return MARKER_NODE;
        }
        return super.createInstanceNode();
    }

    private Cache createInlineCache(T owner, MutableStatistics statistics) {
        assert inlineCache;
        CacheDescriptor<T> descriptor = getDescriptor();
        Class valueType = descriptor.getSignature().getValue();
        Calculable calculable = descriptor.getCalculable();
        if (valueType == boolean.class) {
            return new BooleanInlineCacheImpl(owner, (BooleanCalculatable) calculable, statistics);
        }
        if (valueType == byte.class) {
            return new ByteInlineCacheImpl(owner, (ByteCalculatable) calculable, statistics);
        }
        if (valueType == short.class) {
            return new ShortInlineCacheImpl(owner, (ShortCalculatable) calculable, statistics);
        }
        if (valueType == char.class) {
            return new CharacterInlineCacheImpl(owner, (CharacterCalculatable) calculable, statistics);
        }
        if (valueType == int.class) {
            return new IntInlineCacheImpl(owner, (IntCalculatable) calculable, statistics);
        }
        if (valueType == long.class) {
            return new LongInlineCacheImpl(owner, (LongCalculatable) calculable, statistics);
        }
        if (valueType == float.class) {
            return new FloatInlineCacheImpl(owner, (FloatCalculatable) calculable, statistics);
        }
        if (valueType == double.class) {
            return new DoubleInlineCacheImpl(owner, (DoubleCalculatable) calculable, statistics);
        }
        //noinspection unchecked
        return new ObjectInlineCacheImpl(owner, (ObjectCalculatable) calculable, statistics);
    }

    private Cache createInlineCacheWithDependencyNode(T owner, MutableStatistics statistics) {
        assert inlineCache;
        CacheDescriptor<T> descriptor = getDescriptor();
        Class valueType = descriptor.getSignature().getValue();
        Calculable calculable = descriptor.getCalculable();
        if (valueType == boolean.class) {
            return new BooleanInlineDependencyCache(owner, (BooleanCalculatable) calculable, statistics);
        }
        if (valueType == byte.class) {
            return new ByteInlineDependencyCache(owner, (ByteCalculatable) calculable, statistics);
        }
        if (valueType == short.class) {
            return new ShortInlineDependencyCache(owner, (ShortCalculatable) calculable, statistics);
        }
        if (valueType == char.class) {
            return new CharacterInlineDependencyCache(owner, (CharacterCalculatable) calculable, statistics);
        }
        if (valueType == int.class) {
            return new IntInlineDependencyCache(owner, (IntCalculatable) calculable, statistics);
        }
        if (valueType == long.class) {
            return new LongInlineDependencyCache(owner, (LongCalculatable) calculable, statistics);
        }
        if (valueType == float.class) {
            return new FloatInlineDependencyCache(owner, (FloatCalculatable) calculable, statistics);
        }
        if (valueType == double.class) {
            return new DoubleInlineDependencyCache(owner, (DoubleCalculatable) calculable, statistics);
        }
        //noinspection unchecked
        return new ObjectInlineDependencyCache(owner, (ObjectCalculatable) calculable, statistics);
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

    private static class MarkerDependencyNode implements DependencyNode {
        @Override
        public Reference<DependencyNode> getSelfReference() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visitDependantNodes(DependencyNodeVisitor visitor) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void appendNodes(TIdentityHashSet<CleaningNode> elements) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trackDependency(DependencyNode node) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addNode(@Nonnull CleaningNode cache) {
            // do nothing: cache itself is DependencyNode
        }
    }
}
