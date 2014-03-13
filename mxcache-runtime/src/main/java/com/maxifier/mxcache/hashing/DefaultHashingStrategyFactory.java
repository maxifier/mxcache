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
     * Эта реализация поддерживает аннотаци  
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
                    // массивы по-умолчанию сравниваются на ссылочное равенство
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
     * Этот метод должен создавать экземпляр заданного класса стратегии хэширования.
     * Перекройте этот метод, если требуется нестандартная инициализация для стратегии (например, используется singleton).
     * Метод не должен выбрасывать исключения, даже если создать не удалось.
     * Реализация обращается к InstanceProvider.
     * @param context cache context
     * @param strategyClass класс стратегии
     * @return экземпляр заданного класса стратегии; null, если этого сделать не удалось.
     */
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
