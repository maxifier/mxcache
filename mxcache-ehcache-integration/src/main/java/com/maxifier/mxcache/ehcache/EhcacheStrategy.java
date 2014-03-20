/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.ehcache;

import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.impl.StorageBasedCacheManager;
import com.maxifier.mxcache.provider.*;
import com.maxifier.mxcache.provider.CacheManager;
import gnu.trove.THashMap;
import net.sf.ehcache.CacheException;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * EhcacheStrategy
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class EhcacheStrategy implements CachingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(EhcacheStrategy.class);

    private static final StrategyProperty<String> CONFIG_URL_PROPERTY = StrategyProperty.create("ehcache.configURL.url", String.class, UseEhcache.class, "configURL");
    public static final String CLASSPATH_PREFIX = "classpath://";

    private final net.sf.ehcache.CacheManager cacheManager = new net.sf.ehcache.CacheManager();

    private final Map<String, net.sf.ehcache.CacheManager> byConfiguration = new THashMap<String, net.sf.ehcache.CacheManager>();

    @Nonnull
    @Override
    public <T> CacheManager<T> getManager(CacheContext context, final CacheDescriptor<T> descriptor) {
        if (descriptor.getKeyType() == null) {
            throw new IllegalArgumentException("Ehcache should use key");
        }
        String configURL = descriptor.getProperty(CONFIG_URL_PROPERTY);
        if (configURL.isEmpty()) {
            return createDefaultCacheManager(context, descriptor);
        }
        synchronized (byConfiguration) {
            net.sf.ehcache.CacheManager cacheManager = byConfiguration.get(configURL);
            if (cacheManager == null) {
                try {
                    URL url;
                    if (configURL.startsWith(CLASSPATH_PREFIX)) {
                        String path = configURL.substring(CLASSPATH_PREFIX.length());
                        url = descriptor.getOwnerClass().getClassLoader().getResource(path);
                    } else {
                        url = new URL(configURL);
                    }
                    cacheManager = new net.sf.ehcache.CacheManager(url);
                } catch (CacheException e) {
                    logger.error("Cannot load ehcache configuration", e);
                    return createDefaultCacheManager(context, descriptor);
                } catch (MalformedURLException e) {
                    logger.error("Invalid ehcache configURL url", e);
                    return createDefaultCacheManager(context, descriptor);
                }
                byConfiguration.put(configURL, cacheManager);
            }
            return createCacheManager(context, descriptor, cacheManager);
        }
    }

    private <T> StorageBasedCacheManager<T> createCacheManager(CacheContext context, CacheDescriptor<T> descriptor, net.sf.ehcache.CacheManager cacheManager) {
        return new StorageBasedCacheManager<T>(context, descriptor, new EhcacheStorageFactory<T>(descriptor, cacheManager));
    }

    private <T> CacheManager<T> createDefaultCacheManager(CacheContext context, CacheDescriptor<T> descriptor) {
        return createCacheManager(context, descriptor, cacheManager);
    }

}
