/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.config.ResourceConfig;
import com.maxifier.mxcache.resource.MxResource;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class MxResourceFactory {
    private static final Map<String, MxResource> RESOURCES = new THashMap<String, MxResource>();

    static {
        for (ResourceConfig resourceConfig : CacheFactory.getConfiguration().getResources()) {
            String name = resourceConfig.getName();
            RESOURCES.put(name, new MxStaticResource(name));
        }
    }

    private MxResourceFactory() {
    }

    /**
     * It is guaranteed that this method will always return the same object for a single id.
     * I.e. resources are singletons.
     * Use @{link #createResource(Object,String)} to create non-singleton resources.
     *
     * @param id resource name
     *
     * @return resource with given name
     */
    public static synchronized MxResource getResource(@Nonnull String id) {
        MxResource resource = RESOURCES.get(id);
        if (resource == null) {
            resource = new MxStaticResource(id);
            RESOURCES.put(id, resource);
        }
        return resource;
    }

    public static MxResource createResource(@Nonnull Object owner, @Nonnull String id) {
        return new MxBoundResource(owner, id);
    }

    public static synchronized Set<MxResource> getAllResources() {
        return Collections.unmodifiableSet(new THashSet<MxResource>(RESOURCES.values()));
    }
}
