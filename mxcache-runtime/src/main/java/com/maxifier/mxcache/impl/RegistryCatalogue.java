/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
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
class RegistryCatalogue {
    private final CacheDescriptor descriptor;
    private final Map<Class<?>, RegistryEntry> byOwnerClass = new THashMap<Class<?>, RegistryEntry>();

    RegistryCatalogue(CacheDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Nonnull
    CacheDescriptor getDescriptor() {
        return descriptor;
    }

    @Nonnull
    RegistryEntry forClass(Class<?> ownerClass) {
        RegistryEntry entry = byOwnerClass.get(ownerClass);
        if (entry == null) {
            entry = new RegistryEntry(ownerClass, descriptor);
            byOwnerClass.put(ownerClass, entry);
        }
        return entry;
    }

    void addManagers(List<CacheManager> res) {
        for (RegistryEntry registryEntry : byOwnerClass.values()) {
            res.addAll(registryEntry.getManagers());
        }
    }
}
