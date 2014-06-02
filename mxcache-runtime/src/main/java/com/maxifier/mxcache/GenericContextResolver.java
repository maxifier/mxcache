/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import com.maxifier.mxcache.context.CacheContext;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
class GenericContextResolver implements ContextResolver<Object> {
    private final Map<Class, Collection<ContextResolver>> resolverMapping;

    @SuppressWarnings( "unchecked" )
    public GenericContextResolver(Iterable<ContextResolver> resolvers) {
        resolverMapping = new THashMap<Class, Collection<ContextResolver>>();
        for (ContextResolver resolver : resolvers) {
            Class streamClass = resolver.getContextOwnerClass();
            Collection<ContextResolver> list = resolverMapping.get(streamClass);
            if (list == null) {
                resolverMapping.put(streamClass, Collections.singleton(resolver));
            } else {
                if (list.size() == 1) {
                    list = new ArrayList<ContextResolver>(list);
                    resolverMapping.put(streamClass, list);
                }
                list.add(resolver);
            }
        }
    }

    public GenericContextResolver() {
        this(ServiceLoader.load(ContextResolver.class));
    }

    @Override
    public Class<Object> getContextOwnerClass() {
        return Object.class;
    }

    @SuppressWarnings( { "unchecked" })
    @Override
    public CacheContext getContext(Object owner) {
        Class<?> cls = owner.getClass();
        for (ContextResolver resolver : getResolvers(cls)) {
            CacheContext res = resolver.getContext(owner);
            if (res != null) {
                return res;
            }
        }
        return CacheFactory.getDefaultContext();
    }

    private synchronized Collection<ContextResolver> getResolvers(Class<?> streamClass) {
        Collection<ContextResolver> res = resolverMapping.get(streamClass);
        if (res != null) {
            return res;
        }
        res = createResolvers(streamClass);
        resolverMapping.put(streamClass, res);
        return res;
    }

    @SuppressWarnings("unchecked")
    private Collection<ContextResolver> createResolvers(Class<?> streamClass) {
        Collection<ContextResolver> res = new THashSet<ContextResolver>();
        Class superclass = streamClass.getSuperclass();
        if (superclass != null) {
            res.addAll(getResolvers(superclass));
        }
        for (Class intf : streamClass.getInterfaces()) {
            res.addAll(getResolvers(intf));
        }
        if (res.isEmpty()) {
            return Collections.emptyList();
        }
        if (res.size() == 1) {
            return Collections.singleton(res.iterator().next());
        }
        return res;
    }
}
