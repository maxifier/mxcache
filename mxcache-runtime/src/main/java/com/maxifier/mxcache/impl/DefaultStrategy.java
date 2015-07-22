/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.hashing.DefaultHashingStrategyFactory;
import com.maxifier.mxcache.hashing.HashingStrategyFactory;
import com.maxifier.mxcache.provider.*;
import com.maxifier.mxcache.provider.CacheManager;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * Default caching strategy.
 * Delegates calls to storage factory (by default it is {@link StorageBasedCacheManager})
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class DefaultStrategy implements CachingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(DefaultStrategy.class);

    private static final String CANNOT_INSTANTIATE_MESSAGE = "Cannot instantiate StorageFactory for %s (%s)";

    private static final DefaultStrategy INSTANCE = new DefaultStrategy();

    public static DefaultStrategy getInstance() {
        return INSTANCE;
    }

    private final HashingStrategyFactory hashingStrategyFactory;

    private DefaultStrategy() {
        hashingStrategyFactory = DefaultHashingStrategyFactory.getInstance();
    }

    public DefaultStrategy(HashingStrategyFactory hashingStrategyFactory) {
        this.hashingStrategyFactory = hashingStrategyFactory;
    }

    @Nonnull
    @Override
    public CacheManager getManager(CacheContext context, Class<?> ownerClass, CacheDescriptor descriptor) {
        return new StorageBasedCacheManager(context, ownerClass, descriptor, new DefaultStorageFactory(context, hashingStrategyFactory, descriptor));
    }

    /**
     * Sometimes it is usefull to create a storage on top of another storage. Use this method to get instance of
     * next factory in chain.
     * <p>This method will first try to create factory of given type, but if attempt fails it will create default
     * factory.
     * @param context context of creation
     * @param descriptor descriptor of cache
     * @param storageFactory requires storage factory class
     * @return storage factory instance.
     */
    public StorageFactory getStorageFactory(CacheContext context, CacheDescriptor descriptor, Class<? extends StorageFactory> storageFactory) {
        //noinspection RedundantCast
        if (((Class)storageFactory) != DefaultStorageFactory.class) {
            if (StorageFactory.class.isAssignableFrom(storageFactory)) {
                Constructor<? extends StorageFactory> ctor = CustomStorageFactory.getCustomConstructor(storageFactory);
                Object[] arguments = CustomStorageFactory.createArguments(ctor, context, descriptor);

                try {
                    return ctor.newInstance(arguments);
                } catch (Exception e) {
                    logger.error(String.format(CANNOT_INSTANTIATE_MESSAGE, descriptor, storageFactory), e);
                }
            } else {
                logger.error("Invalid cache manager for " + descriptor + " (" + storageFactory + " is not StorageFactory)");
            }
        }
        return new DefaultStorageFactory(context, hashingStrategyFactory, descriptor);
    }
}
