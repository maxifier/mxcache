package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.mbean.CacheControl;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.CacheManager;
import com.maxifier.mxcache.provider.CacheProvider;
import gnu.trove.THashMap;

import java.util.*;
import java.lang.management.ManagementFactory;

import com.maxifier.mxcache.caches.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.management.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 03.03.2010
 * Time: 17:59:10
 */
public class CacheProviderImpl implements CacheProvider {
    private static final Logger logger = LoggerFactory.getLogger(CacheProviderImpl.class);

    private final Map<CacheId, RegistryEntry> registry = new THashMap<CacheId, RegistryEntry>();

    public CacheProviderImpl() {
        this(true);
    }

    public CacheProviderImpl(boolean needsMBean) {
        if (needsMBean) {
            registerMBean();
        }
    }

    private void registerMBean() {
        try {
            ManagementFactory.getPlatformMBeanServer().registerMBean(new CacheControl(this), new ObjectName("com.maxifier.mxcache:service=CacheControl"));
        } catch (JMException e) {
            logger.error("Cannot register MxCache mbean", e);
        }
    }

    @Override
    public synchronized <T> void registerCache(Class<T> cacheOwner, int cacheId, Class key, Class value, String group, String[] tags, Object calculable, String methodName, String methodDesc, String cacheName) {
        if (logger.isTraceEnabled()) {
            logger.trace("Register: owner = {}, method = {}, name = {}, id = {}, type = {} -> {}, group = {}, tags = {}", new Object[] {cacheOwner, methodName + methodDesc, cacheName, cacheId, key, value, group, Arrays.toString(tags)});
        }
        CacheDescriptor<T> descriptor = new CacheDescriptor<T>(cacheOwner, cacheId, key, value, calculable, methodName, methodDesc, cacheName, group, tags, null);
        RegistryEntry<T> entry = new RegistryEntry<T>(descriptor);
        registry.put(new CacheId(cacheOwner, cacheId), entry);
    }

    @Override
    public synchronized Cache createCache(@NotNull Class cacheOwner, int cacheId, @Nullable Object instance, CacheContext context) {
        if (logger.isTraceEnabled()) {
            logger.trace("createCache({}, {}, {})", new Object[] {cacheOwner, cacheId, instance});
        }

        RegistryEntry registryEntry = registry.get(new CacheId(cacheOwner, cacheId));
        if (registryEntry == null) {
            throw new IllegalStateException("Unknown cache: " + cacheOwner + " # " + cacheId);
        }
        //noinspection unchecked
        return registryEntry.createCache(context, instance);
    }

    @Override
    public synchronized CacheDescriptor getDescriptor(CacheId id) {
        if (id == null) {
            return null;
        }
        RegistryEntry entry = registry.get(id);
        if (entry == null) {
            return null;
        }
        return entry.getDescriptor();
    }

    @Override
    public synchronized List<CacheManager> getCaches() {
        List<CacheManager> res = new ArrayList<CacheManager>(registry.size());
        for (RegistryEntry<?> registryEntry : registry.values()) {
            res.addAll(registryEntry.getManagers());
        }
        return res;
    }
}
