/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.caches.Calculable;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.mbean.CacheControl;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.CacheManager;
import com.maxifier.mxcache.provider.CacheProvider;
import com.maxifier.mxcache.provider.CacheProviderInterceptor;
import gnu.trove.map.hash.THashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.InstanceAlreadyExistsException;
import javax.management.JMException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class CacheProviderImpl implements CacheProvider {
    private static final Logger logger = LoggerFactory.getLogger(CacheProviderImpl.class);

    private final Map<CacheId, RegistryCatalogue> registry = new THashMap<CacheId, RegistryCatalogue>();
    private final CacheProviderInterceptorChain interceptorChain = new CacheProviderInterceptorChain();

    public CacheProviderImpl() {
        this(true);
    }

    public CacheProviderImpl(boolean needsMBean) {
        if (needsMBean) {
            registerMBean(new CacheControl(this), "com.maxifier.mxcache:service=CacheControl");
        }
    }

    @Override
    public void intercept(CacheProviderInterceptor interceptor) {
        interceptorChain.add(interceptor);
    }

    @Override
    public boolean removeInterceptor(CacheProviderInterceptor interceptor) {
        return interceptorChain.remove(interceptor);
    }

    public static void registerMBean(Object mbean, String name) {
        try {
            try {
                ManagementFactory.getPlatformMBeanServer().registerMBean(mbean, new ObjectName(name));
            } catch (InstanceAlreadyExistsException e) {
                // if there are two or more instances of MxCache on classloader we don't want to fail
                logger.warn("MxCache MBean is already registered ({}), will use unique name fallback", name, e.getMessage());
                ManagementFactory.getPlatformMBeanServer().registerMBean(mbean, new ObjectName(name + "-" + UUID.randomUUID()));
            }
        } catch (JMException e) {
            logger.error("Cannot register MxCache mbean", e);
        }
    }

    @Override
    public synchronized <T> void registerCache(Class<T> declaringClass, int cacheId, Class key, Class value, String group, String[] tags, Calculable calculable, String methodName, String methodDesc, String cacheName) {
        if (logger.isTraceEnabled()) {
            logger.trace("Register: owner = {}, method = {}, name = {}, id = {}, type = {} -> {}, group = {}, tags = {}", new Object[] {declaringClass, methodName + methodDesc, cacheName, cacheId, key, value, group, Arrays.toString(tags)});
        }
        CacheDescriptor<T> descriptor = new CacheDescriptor<T>(declaringClass, cacheId, key, value, calculable, methodName, methodDesc, cacheName, group, tags, null);
        descriptor = interceptorChain.registerCache(descriptor);
        RegistryCatalogue<T> entry = new RegistryCatalogue<T>(descriptor);
        registry.put(new CacheId(declaringClass, cacheId), entry);
    }

    @Override
    public synchronized Cache createCache(@Nonnull Class declaringClass, int cacheId, @Nullable Object instance, CacheContext context) {
        if (logger.isTraceEnabled()) {
            logger.trace("createCache({}, {}, {})", new Object[] {declaringClass, cacheId, instance});
        }

        RegistryCatalogue catalogue = registry.get(new CacheId(declaringClass, cacheId));
        if (catalogue == null) {
            throw new IllegalStateException("Unknown cache: " + declaringClass + " # " + cacheId);
        }
        //noinspection unchecked
        RegistryEntry registryEntry = catalogue.forClass(instance == null ? declaringClass : instance.getClass());
        Cache res = registryEntry.createCache(context, instance);
        res = interceptorChain.createCache(registryEntry, instance, context, res);
        return res;
    }

    @Override
    public synchronized CacheDescriptor getDescriptor(CacheId id) {
        if (id == null) {
            return null;
        }
        RegistryCatalogue entry = registry.get(id);
        if (entry == null) {
            return null;
        }
        return entry.getDescriptor();
    }

    @Override
    public synchronized List<CacheManager> getCaches() {
        List<CacheManager> res = new ArrayList<CacheManager>(registry.size());
        for (RegistryCatalogue<?> catalogue : registry.values()) {
            catalogue.addManagers(res);
        }
        return res;
    }

}
