/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.Signature;
import com.maxifier.mxcache.provider.StorageFactory;
import com.maxifier.mxcache.storage.Storage;
import com.maxifier.mxcache.transform.Ignore;
import com.maxifier.mxcache.transform.SoftKey;
import com.maxifier.mxcache.transform.WeakKey;
import gnu.trove.list.array.TIntArrayList;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
public class DefaultStorageFactory implements StorageFactory {
    private static final String CACHES_PACKAGE = "com.maxifier.mxcache.impl.caches.def.";

    private final Class implementation;

    private final Constructor<? extends Storage> storageConstructor;
    private final int[] tupleIndices;

    DefaultStorageFactory(CacheDescriptor descriptor) {
        Signature transformedSignature = descriptor.getTransformedSignature().overrideValue(Object.class);
        if (!transformedSignature.hasKeys()) {
            implementation = transformedSignature.getImplementationClass(CACHES_PACKAGE, "StorageImpl");
            tupleIndices = null;
        } else {
            TIntArrayList p = getReferenceKeys(descriptor);
            if (p.isEmpty()) {
                implementation = transformedSignature.getImplementationClass(CACHES_PACKAGE, "TroveStorage");
                tupleIndices = null;
            } else if (transformedSignature.getKeyCount() == 1) {
                implementation = transformedSignature.getImplementationClass(CACHES_PACKAGE, "WeakTroveStorage");
                tupleIndices = null;
            } else {
                implementation = findClass(CACHES_PACKAGE + "Tuple" + Signature.toString(descriptor.getValueType()) + "WeakTroveStorage");
                tupleIndices = p.toArray();
            }
        }
        storageConstructor = getStorageConstructor();
    }

    private Class findClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings({ "unchecked" })
    private Constructor<? extends Storage> getStorageConstructor() {
        int desiredCount = (tupleIndices == null) ? 0 : 1;
        for (Constructor<?> constructor : implementation.getConstructors()) {
            if (constructor.getParameterTypes().length == desiredCount) {
                return (Constructor<? extends Storage>) constructor;
            }
        }
        throw new IllegalStateException("No corresponding constructor at " + implementation);
    }

    @Nonnull
    @Override
    public Storage createStorage(Object owner) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        if (tupleIndices == null) {
            return storageConstructor.newInstance();
        } else {
            return storageConstructor.newInstance((Object) tupleIndices);
        }
    }

    private TIntArrayList getReferenceKeys(CacheDescriptor descriptor) {
        Annotation[][] ann = descriptor.getMethod().getParameterAnnotations();
        TIntArrayList p = new TIntArrayList(ann.length);
        int pos = 0;
        for (Annotation[] annotations : ann) {
            boolean ref = false;
            boolean ignored = false;
            for (Annotation a : annotations) {
                if ((a instanceof WeakKey) || (a instanceof SoftKey)) {
                    ref = true;
                }
                if (a instanceof Ignore) {
                    ignored = true;
                }
            }
            if (!ignored) {
                if (ref) {
                    p.add(pos);
                }
                pos++;
            }
        }
        return p;
    }

    @Override
    public String getImplementationDetails() {
        return storageConstructor.getDeclaringClass().getCanonicalName();
    }
}
