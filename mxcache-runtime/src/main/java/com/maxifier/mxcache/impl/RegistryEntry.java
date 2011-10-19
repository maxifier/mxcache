package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.NoSuchInstanceException;
import com.maxifier.mxcache.UseStorage;
import com.maxifier.mxcache.UseStorageFactory;
import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.provider.*;
import com.maxifier.mxcache.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 24.06.2010
* Time: 11:03:20
*/
class RegistryEntry<T> {
    private static final Logger logger = LoggerFactory.getLogger(RegistryEntry.class);

    private static final StrategyProperty<Class> STORAGE_FACTORY_PROPERTY = new AnnotationProperty<UseStorageFactory, Class>("storage.factory", Class.class, UseStorageFactory.class) {
        @Override
        public Class getFromAnnotation(UseStorageFactory annotation) {
            return annotation.value();
        }
    };

    private static final StrategyProperty<Class> STORAGE_PROPERTY = new AnnotationProperty<UseStorage, Class>("storage", Class.class, UseStorage.class) {
        @Override
        public Class getFromAnnotation(UseStorage annotation) {
            return annotation.value();
        }
    };

    private final CacheDescriptor<T> descriptor;

    private final CacheManager<T> nullCacheManager;

    private final Map<CacheContext, CacheManager<T>> managers;

    public RegistryEntry(CacheDescriptor<T> descriptor) {
        this.descriptor = descriptor;
        if (descriptor.isDisabled()) {
            nullCacheManager = new NullCacheManager<T>(descriptor);
            managers = null;
        } else {
            nullCacheManager = null;
            managers = new WeakHashMap<CacheContext, CacheManager<T>>();
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

    public synchronized Collection<CacheManager<T>> getManagers() {
        return managers == null ? Collections.<CacheManager<T>>emptySet() : Collections.unmodifiableCollection(managers.values());
    }

    private synchronized CacheManager<T> getManager(CacheContext context) {
        if (nullCacheManager != null) {
            return nullCacheManager;
        }
        CacheManager<T> manager = managers.get(context);
        if (manager == null) {
            try {
                manager = createManager(context);
                managers.put(context, manager);
            } catch (Exception e) {
                logger.error("Cannot instantiate cache for " + descriptor + ", will use default", e);
                return DefaultStrategy.getInstance().getManager(context, descriptor);
            }
        }
        return manager;
    }

    public Cache createCache(CacheContext context, T instance) {
        try {
            return getManager(context).createCache(instance);
        } catch (RuntimeException e) {
            return DefaultStrategy.getInstance().getManager(context, descriptor).createCache(instance);
        }
    }

    private void checkTooManyStrategies(Class<StorageFactory<T>> storageFactoryClass, Class<Storage> storageClass, Class<? extends CachingStrategy> strategyClass) {
        int n = 0;
        StringBuilder message = new StringBuilder();
        if (storageFactoryClass != null) {
            if (n != 0) {
                message.append(", ");
            }
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
    private CacheManager<T> createManager(CacheContext context) {
        Class<StorageFactory<T>> storageFactoryClass = descriptor.getProperty(STORAGE_FACTORY_PROPERTY);
        Class<Storage> storageClass = descriptor.getProperty(STORAGE_PROPERTY);
        Class<? extends CachingStrategy> strategyClass = descriptor.getStrategyClass();

        checkTooManyStrategies(storageFactoryClass, storageClass, strategyClass);

        if (storageFactoryClass != null) {
            return new StorageBasedCacheManager<T>(context, descriptor, DefaultStrategy.getInstance().getStorageFactory(context, descriptor, storageFactoryClass));
        }
        if (storageClass != null) {
            return new StorageBasedCacheManager<T>(context, descriptor, new CustomStorageFactory<T>(context, descriptor, storageClass));
        }
        if (strategyClass == null) {
            strategyClass = DefaultStrategy.class;
        }
        CachingStrategy strategy = getStrategyInstance(context, strategyClass);
        CacheManager<T> manager;
        try {
            manager = strategy.getManager(context, descriptor);
        } catch (RuntimeException e) {
            logger.error("Strategy failed: " + strategy + ", will try default strategy", e);
            return DefaultStrategy.getInstance().getManager(context, descriptor);
        }
        if (manager == null) {
            logger.error("Strategy failed: " + strategy + " returned null manager");
            return DefaultStrategy.getInstance().getManager(context, descriptor);
        }
        return manager;
    }

    public CacheDescriptor<T> getDescriptor() {
        return descriptor;
    }

    @Override
    public String toString() {
        return "RegistryEntry{descriptor=" + descriptor + ", managers=" + (managers == null ? nullCacheManager : managers) + '}';
    }
}
