/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.hashing;

import gnu.trove.strategy.HashingStrategy;
import gnu.trove.strategy.IdentityHashingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * No more extensible - use @HashingStrategy(Your_HashingStrategy_Implementation.class) to do custom hashing strategies.
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class HashingStrategyFactory {
    private static final Logger logger = LoggerFactory.getLogger(HashingStrategyFactory.class);

    /**
     * Resolve hashing strategies for each provided erased type.
     * If definedHSForArgs[i] != null (means it was configured explicitly by annotation),
     * then definedHSForArgs[i] should be used for i-th argument.
     * @return array of hashing strategies classes. result[i] == null means Object.hashCode
     * and Object.equals. For primitives there must be nulls.
     */
    @SuppressWarnings("unused")
    public static HashingStrategy[] createHashingStrategies(Class[] argsTypes, Class<? extends HashingStrategy>[] definedHSForArgs) {
        HashingStrategy[] result = new HashingStrategy[argsTypes.length];
        for (int i = 0; i < result.length; i++) {
            if (argsTypes[i].isPrimitive()) {
                result[i] = null;
                if (definedHSForArgs[i] != null) {
                    logger.error("Argument of primitive type cannot have custom hashing strategy " + definedHSForArgs[i] + "");
                }
            } else if (definedHSForArgs[i] != null) {
                if (IdentityHashingStrategy.class.equals(definedHSForArgs[i])) {
                    result[i] = IdentityHashingStrategy.INSTANCE;
                } else {
                    result[i] = tryDefaultConstructor(definedHSForArgs[i]);
                    if (result[i] == null) {
                        logger.error("Can't instantiate custom HashingStrategy class " + definedHSForArgs[i] + ", will use default strategy");
                        if (argsTypes[i].isArray()) { // default for arrays
                            result[i] = getArrayHashingStrategy(argsTypes[i]);
                        }
                    }
                }
            } else if (argsTypes[i].isArray()) {
                result[i] = getArrayHashingStrategy(argsTypes[i]);
            } else {
                result[i] = null;
            }
        }
        return result;
    }

    @Nullable
    private static HashingStrategy tryDefaultConstructor(Class<? extends HashingStrategy> hsClass) {
        try {
            Constructor<? extends HashingStrategy> ctor = hsClass.getConstructor();
            return ctor.newInstance();
        } catch (InvocationTargetException e) {
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private static HashingStrategy getArrayHashingStrategy(Class paramType) {
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
        throw new AssertionError();
    }
}
