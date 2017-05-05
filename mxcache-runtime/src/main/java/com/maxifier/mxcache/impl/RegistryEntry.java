/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.NoSuchInstanceException;
import com.maxifier.mxcache.PublicAPI;
import com.maxifier.mxcache.UseStorage;
import com.maxifier.mxcache.UseStorageFactory;
import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.provider.*;
import com.maxifier.mxcache.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

/**
 * RegistryEntry is related to a certain cached method. It is a wrapper to {@link com.maxifier.mxcache.provider.CacheDescriptor}
 * that manages all {@link CacheManager} for this cache in all contexts. It is also responsible for handling exceptions
 * in cache manager: if cache manager fails to create a cache instance, it will substitute default cache manager
 * instead.
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
@ThreadSafe
public class RegistryEntry implements CacheContext.ContextRelatedItem<CacheManager> {
    private static final Logger logger = LoggerFactory.getLogger(RegistryEntry.class);

    private static final StrategyProperty<Class> STORAGE_FACTORY_PROPERTY = new AnnotationProperty<UseStorageFactory, Class>("storage.factory", Class.class, UseStorageFactory.class) {
        @Override
        public Class getFromAnnotation(@Nonnull UseStorageFactory annotation) {
            return annotation.value();
        }
    };

    private static final StrategyProperty<Class> STORAGE_PROPERTY = new AnnotationProperty<UseStorage, Class>("storage", Class.class, UseStorage.class) {
        @Override
        public Class getFromAnnotation(@Nonnull UseStorage annotation) {
            return annotation.value();
        }
    };

    private final Class<?> ownerClass;

    private final CacheDescriptor descriptor;

    private final CacheManager nullCacheManager;

    private final WeakHashMap<CacheContext, Void> relatedContexts;

    public RegistryEntry(Class<?> ownerClass, CacheDescriptor descriptor) {
        this.ownerClass = ownerClass;
        this.descriptor = descriptor;
        if (descriptor.isDisabled()) {
            nullCacheManager = new NullCacheManager(ownerClass, descriptor);
            relatedContexts = null;
        } else {
            nullCacheManager = null;
            relatedContexts = new WeakHashMap<CacheContext, Void>();
        }
    }

    private CachingStrategy getStrategyInstance(CacheContext context, Class<? extends CachingStrategy> strategy) {
        if (strategy == null || strategy == DefaultStrategy.class) {
            return DefaultStrategy.getInstance();
        }
        try {
            return context.getInstanceProvider().forClass(strategy);
        } catch (NoSuchInstanceException e) {
            logger.error("Cannot acquire instance of " + strategy, e);
            return DefaultStrategy.getInstance();
        }
    }

    /**
     * @return all cache managers that were created in all context by this entry.
     */
    @PublicAPI
    public synchronized Collection<CacheManager> getManagers() {
        if (relatedContexts == null) {
            return Collections.emptySet();
        }
        List<CacheManager> managers = new ArrayList<CacheManager>();
        for (CacheContext context : relatedContexts.keySet()) {
            CacheManager manager = context.getRelated(this);
            if (manager != null) {
                managers.add(manager);
            }
        }
        return Collections.unmodifiableCollection(managers);
    }

    /**
     * Finds cache manager for descriptor in given context
     * @param context cache context
     * @return cache manager.
     */
    @PublicAPI
    public synchronized CacheManager getManager(CacheContext context) {
        if (nullCacheManager != null) {
            return nullCacheManager;
        }
        relatedContexts.put(context, null);
        CacheManager manager = context.getRelated(this);
        if (manager == null) {
            try {
                manager = createManager(context);
                context.setRelated(this, manager);
            } catch (Exception e) {
                logger.error("Cannot instantiate cache for " + descriptor + ", will use default", e);
                return DefaultStrategy.getInstance().getManager(context, ownerClass, descriptor);
            }
        }
        return manager;
    }

    /**
     * Creates a new instance of cache on each call.
     * @param context associated context
     * @param instance cache instance
     * @return create cache instance.
     */
    @PublicAPI
    public Cache createCache(CacheContext context, @Nullable Object instance) {
        try {
            return getManager(context).createCache(instance);
        } catch (RuntimeException e) {
            logger.error("Cannot create cache for " + instance + ", DefaultStrategy will be used", e);
            return DefaultStrategy.getInstance().getManager(context, ownerClass, descriptor).createCache(instance);
        }
    }

    private void checkTooManyStrategies(Class<StorageFactory> storageFactoryClass, Class<Storage> storageClass, Class<? extends CachingStrategy> strategyClass) {
        int n = 0;
        StringBuilder message = new StringBuilder();
        if (storageFactoryClass != null) {
            message.append("storageFactory = ").append(storageFactoryClass);
            n++;
        }
        if (storageClass != null) {
            if (n != 0) {
                message.append(", ");
            }
            message.append("storage = ").append(storageClass);
            n++;
        }
        if (strategyClass != null) {
            if (n != 0) {
                message.append(", ");
            }
            message.append("strategy = ").append(strategyClass);
            n++;
        }
        if (n > 1) {
            logger.error("Too many strategy assignments: " + message + " at " + descriptor);
        }
    }

    @SuppressWarnings({ "unchecked" })
    private CacheManager createManager(CacheContext context) {
        Class<StorageFactory> storageFactoryClass = descriptor.getProperty(STORAGE_FACTORY_PROPERTY);
        Class<Storage> storageClass = descriptor.getProperty(STORAGE_PROPERTY);
        Class<? extends CachingStrategy> strategyClass = descriptor.getStrategyClass();

        checkTooManyStrategies(storageFactoryClass, storageClass, strategyClass);

        if (storageFactoryClass != null) {
            return new StorageBasedCacheManager(context, ownerClass, descriptor, DefaultStrategy.getInstance().getStorageFactory(context, descriptor, storageFactoryClass));
        }
        if (storageClass != null) {
            return new StorageBasedCacheManager(context, ownerClass, descriptor, new CustomStorageFactory(context, descriptor, storageClass));
        }
        if (strategyClass == null) {
            strategyClass = DefaultStrategy.class;
        }
        CachingStrategy strategy = getStrategyInstance(context, strategyClass);
        CacheManager manager;
        try {
            manager = strategy.getManager(context, ownerClass, descriptor);
        } catch (RuntimeException e) {
            logger.error("Strategy failed: " + strategy + ", will try default strategy", e);
            return DefaultStrategy.getInstance().getManager(context, ownerClass, descriptor);
        }
        if (manager == null) {
            logger.error("Strategy failed: " + strategy + " returned null manager");
            return DefaultStrategy.getInstance().getManager(context, ownerClass, descriptor);
        }
        return manager;
    }

    /**
     * @return descriptor related to this entry
     */
    @PublicAPI
    public CacheDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public String toString() {
        return "RegistryEntry{descriptor=" + descriptor + ", managers=" + (nullCacheManager == null ? getManagers() : nullCacheManager) + '}';
    }
}
