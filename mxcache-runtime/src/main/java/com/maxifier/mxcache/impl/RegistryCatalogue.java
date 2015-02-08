/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.CacheManager;
import gnu.trove.map.hash.THashMap;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.List;
import java.util.Map;

/**
 * RegistryCatalogue
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2015-07-21 19:18)
 */
@NotThreadSafe
class RegistryCatalogue<T> {
    private final CacheDescriptor<T> descriptor;
    private final Map<Class<?>, RegistryEntry<T>> byOwnerClass = new THashMap<Class<?>, RegistryEntry<T>>();

    RegistryCatalogue(CacheDescriptor<T> descriptor) {
        this.descriptor = descriptor;
    }

    @Nonnull
    CacheDescriptor<T> getDescriptor() {
        return descriptor;
    }

    @Nonnull
    RegistryEntry<T> forClass(Class<?> ownerClass) {
        RegistryEntry<T> entry = byOwnerClass.get(ownerClass);
        if (entry == null) {
            entry = new RegistryEntry<T>(ownerClass, descriptor);
            byOwnerClass.put(ownerClass, entry);
        }
        return entry;
    }

    void addManagers(List<CacheManager> res) {
        for (RegistryEntry<T> registryEntry : byOwnerClass.values()) {
            res.addAll(registryEntry.getManagers());
        }
    }
}
