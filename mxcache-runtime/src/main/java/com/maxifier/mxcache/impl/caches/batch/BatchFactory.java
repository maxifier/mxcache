/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.batch;

import com.maxifier.mxcache.impl.caches.def.ObjectObjectTroveStorage;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.StorageFactory;
import com.maxifier.mxcache.storage.Storage;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class BatchFactory implements StorageFactory {
    private final CacheDescriptor descriptor;

    public BatchFactory(CacheDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public Storage createStorage(Object owner) {
        ObjectObjectTroveStorage storage = new ObjectObjectTroveStorage();

        Class keyType = descriptor.getKeyType();
        Class valueType = descriptor.getValueType();

        KeyStrategy keyStrategy;
        if (List.class.isAssignableFrom(keyType)) {
            keyStrategy = ListKeyStrategy.getInstance();
        } else if (Collection.class.isAssignableFrom(keyType)) {
            keyStrategy = CollectionKeyStrategy.getInstance();
        } else if (keyType.isArray() && !keyType.getComponentType().isPrimitive()) {
            keyStrategy = ArrayKeyStrategy.getInstance();
        } else {
            throw new UnsupportedOperationException("Unsupported batch cache key type: " + keyType);
        }
        ValueStrategy valueStrategy;
        if (List.class.isAssignableFrom(valueType)) {
            valueStrategy = ListValueStrategy.getInstance();
        } else if (Map.class.isAssignableFrom(valueType)) {
            valueStrategy = MapValueStrategy.getInstance();
        } else if (valueType.isArray() && !valueType.getComponentType().isPrimitive()) {
            valueStrategy = ArrayValueStrategy.getInstance();
        } else {
            throw new UnsupportedOperationException("Unsupported batch cache value type: " + keyType);
        }

        if (valueStrategy.requiresOrder() && !keyStrategy.isStableOrder()) {
            throw new UnsupportedOperationException("Incompatible key for batch cache: " + keyType + " and " + valueType);
        }

        return new BatchObjectObjectStorage(storage, keyStrategy, valueStrategy, valueType);
    }

    @Override
    public String getImplementationDetails() {
        return "<Batch cache>";
    }
}
