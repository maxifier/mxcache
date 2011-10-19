package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.hashing.DefaultHashingStrategyFactory;
import com.maxifier.mxcache.hashing.HashingStrategyFactory;
import com.maxifier.mxcache.provider.*;
import com.maxifier.mxcache.provider.CacheManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 22.04.2010
 * Time: 18:28:26
 * <p>
 * Стратегия кэширования по умолчанию.
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

    @NotNull
    @Override
    public <T> CacheManager<T> getManager(CacheContext context, CacheDescriptor<T> descriptor) {
        return new StorageBasedCacheManager<T>(context, descriptor, new DefaultStorageFactory<T>(context, hashingStrategyFactory, descriptor));
    }

    /**
     * Sometimes it is usefull to create a storage on top of another storage. Use this method to get instance of
     * next factory in chain.
     * <p>This method will first try to create factory of given type, but if attempt fails it will create default
     * factory.
     * @param context context of creation
     * @param descriptor descriptor of cache
     * @param storageFactory requires storage factory class
     * @param <T> type of owner
     * @return storage factory instance.
     */
    public <T> StorageFactory<T> getStorageFactory(CacheContext context, CacheDescriptor<T> descriptor, Class<? extends StorageFactory<T>> storageFactory) {
        //noinspection RedundantCast
        if (((Class)storageFactory) != DefaultStorageFactory.class) {
            if (StorageFactory.class.isAssignableFrom(storageFactory)) {
                Constructor<? extends StorageFactory<T>> ctor = CustomStorageFactory.getCustomConstructor(storageFactory);
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
        return new DefaultStorageFactory<T>(context, hashingStrategyFactory, descriptor);
    }
}
