/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.hashing;

import com.maxifier.mxcache.NoSuchInstanceException;
import com.maxifier.mxcache.context.CacheContext;
import gnu.trove.TObjectHashingStrategy;
import gnu.trove.TObjectIdentityHashingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * <p>DefaultHashingStrategyFactory. Supports @{@link HashingStrategy} annotations.
 * Compares and hashes arrays by content, not by identity. Supports tuples.</p>
 * <p>Never throws exceptions but outputs an error to the log.</p>
 * <p>You can add custom annotation support in child classes or override strategy creation.</p>
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class DefaultHashingStrategyFactory extends AbstractHashingStrategyFactory {
    private static final Logger logger = LoggerFactory.getLogger(DefaultHashingStrategyFactory.class);

    private static final DefaultHashingStrategyFactory INSTANCE = new DefaultHashingStrategyFactory();

    public static DefaultHashingStrategyFactory getInstance() {
        return INSTANCE;
    }

    private DefaultHashingStrategyFactory() {}

    /**
     * {@inheritDoc}
     * This implementation supports {@link com.maxifier.mxcache.hashing.HashingStrategy} and
     * {@link com.maxifier.mxcache.hashing.IdentityHashing} annotations.
     */
    @Override
    protected Object findStrategyClass(CacheContext context, Class paramType, Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof HashingStrategy) {
                //noinspection unchecked
                return instantiate(context, ((HashingStrategy)annotation).value());
            } else if (annotation instanceof IdentityHashing) {
                if (paramType.isPrimitive()) {
                    throw new UnsupportedOperationException();
                }
                if (paramType.isArray()) {
                    // arrays are compared with referential equality by default
                    return null;
                }
                return new TObjectIdentityHashingStrategy();
            }
        }
        if (paramType.isArray()) {
            return getArrayHashingStrategy(paramType);
        }
        return null;
    }

    /**
     * This method should create an instance of strategy.
     *
     * Override it if you need non-standard behavior (e.g. singleton strategy).
     *
     * Don't throw exception even if creation hasn't succeeded, return null instead.
     *
     * This particular implementation queries {@link com.maxifier.mxcache.InstanceProvider} for instances.
     * @param context cache context
     * @param strategyClass strategy class
     * @return an instance of strategy or null if it can't be instantiated
     */
    @Nullable
    protected <T> T instantiate(CacheContext context, Class<T> strategyClass) {
        try {
            return context.getInstanceProvider().forClass(strategyClass);
        } catch (NoSuchInstanceException e) {
            logger.error("Cannot instantiate strategy of type " + strategyClass, e);
            return null;
        }
    }

    private TObjectHashingStrategy getArrayHashingStrategy(Class paramType) {
        if (paramType == boolean[].class) {
            return BooleanArrayHashingStrategy.getInstance();
        }
        if (paramType == byte[].class) {
            return ByteArrayHashingStrategy.getInstance();
        }
        if (paramType == char[].class) {
            return CharArrayHashingStrategy.getInstance();
        }
        if (paramType == short[].class) {
            return ShortArrayHashingStrategy.getInstance();
        }
        if (paramType == int[].class) {
            return IntArrayHashingStrategy.getInstance();
        }
        if (paramType == long[].class) {
            return LongArrayHashingStrategy.getInstance();
        }
        if (paramType == float[].class) {
            return FloatArrayHashingStrategy.getInstance();
        }
        if (paramType == double[].class) {
            return DoubleArrayHashingStrategy.getInstance();
        }
        if (paramType.isArray()) {
            return ArrayHashingStrategy.getInstance();
        }
        throw new UnsupportedOperationException();
    }
}
