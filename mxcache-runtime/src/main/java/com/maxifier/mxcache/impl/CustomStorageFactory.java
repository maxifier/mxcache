package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.StorageFactory;
import com.maxifier.mxcache.storage.Storage;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 19.11.2010
* Time: 10:40:41
*/
public class CustomStorageFactory<T> implements StorageFactory<T> {
    private static final Object[] EMPTY_ARRAY = {};

    private final Constructor<? extends Storage> constructor;

    private final Object[] arguments;

    public CustomStorageFactory(CacheContext context, CacheDescriptor<T> descriptor, Class<? extends Storage> storageClass) {
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

    @Override
    public Storage createStorage(T owner) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        return constructor.newInstance(arguments);
    }

    @Override
    public String getImplementationDetails() {
        return constructor.getDeclaringClass().getCanonicalName();
    }

    @NotNull
    static <T> Constructor<? extends T> getCustomConstructor(Class<? extends T> cls) {
        Constructor[] constructors = cls.getDeclaredConstructors();

        Constructor<? extends T> res = null;
        int n = 0;
        for (Constructor constructor : constructors) {
            Class<?>[] types = constructor.getParameterTypes();
            if (isAllowedConstructorArgumentTypes(types)) {
                if (res == null || n < types.length) {
                    //noinspection unchecked
                    res = constructor;
                    n = types.length;
                }
            }
        }
        return res;
    }

    static <T> Object[] createArguments(Constructor<? extends T> constructor, CacheContext context, CacheDescriptor<?> descriptor) {
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
