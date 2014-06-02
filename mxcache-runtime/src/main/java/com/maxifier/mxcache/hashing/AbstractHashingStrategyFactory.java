/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.hashing;

import com.maxifier.mxcache.context.CacheContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * AbstractHashingStrategyFactory
 *
 * Extend this class and override method
 * {@link #findStrategyClass(com.maxifier.mxcache.context.CacheContext, Class, java.lang.annotation.Annotation[])}.
 * to add you own hashing-strategy annotations.
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public abstract class AbstractHashingStrategyFactory implements HashingStrategyFactory {
    private static final Logger logger = LoggerFactory.getLogger(AbstractHashingStrategyFactory.class);

    @Override
    public Object createHashingStrategy(CacheContext context, Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length == 0) {
            return null;
        }
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        if (paramTypes.length > 1) {
            return createTupleHashingStrategy(context, paramTypes, paramAnnotations);
        } else {
            return createValueHashingStrategy(context, paramTypes[0], paramAnnotations[0]);
        }
    }

    private Object createValueHashingStrategy(CacheContext context, Class paramType, Annotation[] paramAnnotation) {
        Object strategy = findStrategyClass(context, paramType, paramAnnotation);
        if (strategy == null || !isSuitableStrategy(paramType, strategy)) {
            return null;
        }
        return  strategy;
    }

    private boolean isSuitableStrategy(Class paramType, Object strategy) {
        if (paramType.isPrimitive()) {
            logger.error("Param of type " + paramType + " cannot have strategy");
            return false;
        } else if (!gnu.trove.strategy.HashingStrategy.class.isInstance(strategy)) {
            logger.error("Param of type " + paramType + " cannot have strategy of type " + strategy);
            return false;
        }
        return true;
    }

    private gnu.trove.strategy.HashingStrategy createTupleHashingStrategy(CacheContext context, Class[] paramTypes, Annotation[][] paramAnnotations) {
        Object[] strategies = new Object[paramAnnotations.length];
        int n = 0;
        for (int i = 0; i < paramAnnotations.length; i++) {
            Object strategy = createValueHashingStrategy(context, paramTypes[i], paramAnnotations[i]);
            if (strategy != null) {
                n++;
            }
            strategies[i] = strategy;
        }
        return n > 0 ? new TupleHashingStrategy(strategies) : null;
    }

    /**
     * This method creates an appropriate hashing strategy
     * @param context context of instance
     * @param paramType the type of parameter that would be compared
     * @param annotations parameter annotations to find the necessary
     * @return hashing strategy of null if strategy wasn't found.
     *     It is recommended to log error message and return null if improper annotation or strategy class was specified
     */
    protected abstract Object findStrategyClass(CacheContext context, Class paramType, Annotation[] annotations);
}
