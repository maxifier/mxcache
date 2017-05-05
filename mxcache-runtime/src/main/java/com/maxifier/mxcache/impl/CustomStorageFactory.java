/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.StorageFactory;
import com.maxifier.mxcache.storage.Storage;

import javax.annotation.Nonnull;

import javax.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
public class CustomStorageFactory implements StorageFactory {
    private static final Object[] EMPTY_ARRAY = {};

    private final Constructor<? extends Storage> constructor;

    private final Object[] arguments;

    public CustomStorageFactory(CacheContext context, CacheDescriptor descriptor, Class<? extends Storage> storageClass) {
        if (!Storage.class.isAssignableFrom(storageClass)) {
            throw new IllegalStateException("Storage implementation should extend Storage interface " + storageClass);
        }
        this.constructor = getCustomConstructor(storageClass);
        if (constructor == null) {
            throw new IllegalStateException("Storage should have constructor taking CacheDescriptor, CacheContext or both: " + storageClass);
        }
        this.arguments = createArguments(constructor, context, descriptor);
    }

    private static boolean isAllowedConstructorArgumentType(Class type) {
        return type == CacheDescriptor.class || type == CacheContext.class;
    }

    private static boolean isAllowedConstructorArgumentTypes(Class[] types) {
        for (Class type : types) {
            if (!isAllowedConstructorArgumentType(type)) {
                return false;
            }
        }
        return true;
    }

    @Nonnull
    @Override
    public Storage createStorage(Object owner) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        return constructor.newInstance(arguments);
    }

    @Override
    public String getImplementationDetails() {
        return constructor.getDeclaringClass().getCanonicalName();
    }

    @Nullable
    static <T> Constructor<? extends T> getCustomConstructor(Class<? extends T> cls) {
        Constructor[] constructors = cls.getDeclaredConstructors();

        Constructor<? extends T> res = null;
        int n = 0;
        for (Constructor constructor : constructors) {
            Class<?>[] types = constructor.getParameterTypes();
            if (isAllowedConstructorArgumentTypes(types) && (res == null || n < types.length)) {
                //noinspection unchecked
                res = constructor;
                n = types.length;
            }
        }
        return res;
    }

    static <T> Object[] createArguments(Constructor<? extends T> constructor, CacheContext context, CacheDescriptor descriptor) {
        Class<?>[] types = constructor.getParameterTypes();
        if (types.length == 0) {
            return EMPTY_ARRAY;
        }
        Object[] res = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            assert isAllowedConstructorArgumentType(types[i]);
            res[i] = types[i] == CacheDescriptor.class ? descriptor : context;
        }
        return res;
    }
}
