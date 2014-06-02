/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.ehcache;

import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.StrategyProperty;
import com.maxifier.mxcache.storage.elementlocked.ObjectObjectElementLockedStorage;
import gnu.trove.map.hash.THashMap;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.locking.ExplicitLockingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * EhcacheStorage
 *
 * @see com.maxifier.mxcache.ehcache.UseEhcache
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class EhcacheStorage<E, F> implements ObjectObjectElementLockedStorage<E, F> {
    private static final Logger logger = LoggerFactory.getLogger(EhcacheStorage.class);

    private static final String CLASSPATH_PREFIX = "classpath://";
    private static final StrategyProperty<String> CONFIG_URL_PROPERTY = StrategyProperty.create("ehcache.configURL.url", String.class, UseEhcache.class, "configURL");

    private static final CacheManager DEFAULT_CACHE_MANAGER = new CacheManager();
    private static final Map<String, CacheManager> CONFIGURATION_CACHE = new THashMap<String, CacheManager>();

    private final ExplicitLockingCache underlyingCache;

    private final Lock readLock;
    private final Lock writeLock;

    public EhcacheStorage(CacheDescriptor<?> descriptor) {
        CacheManager cacheManager = getCacheManager(descriptor);
        this.underlyingCache = new ExplicitLockingCache(cacheManager.getCache(descriptor.getCacheName()));

        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    private static CacheManager getCacheManager(CacheDescriptor<?> descriptor) {
        String configURL = descriptor.getProperty(CONFIG_URL_PROPERTY);
        if (configURL.isEmpty()) {
            return DEFAULT_CACHE_MANAGER;
        }
        synchronized (CONFIGURATION_CACHE) {
            CacheManager cacheManager = CONFIGURATION_CACHE.get(configURL);
            if (cacheManager == null) {
                try {
                    URL url = getConfigurationURL(descriptor, configURL);
                    cacheManager = new CacheManager(url);
                } catch (CacheException e) {
                    logger.error("Cannot load ehcache configuration", e);
                    cacheManager = DEFAULT_CACHE_MANAGER;
                } catch (MalformedURLException e) {
                    logger.error("Invalid ehcache configURL url", e);
                    cacheManager = DEFAULT_CACHE_MANAGER;
                }
                CONFIGURATION_CACHE.put(configURL, cacheManager);
            }
            return cacheManager;
        }
    }

    private static URL getConfigurationURL(CacheDescriptor<?> descriptor, String configURL) throws MalformedURLException {
        if (configURL.startsWith(CLASSPATH_PREFIX)) {
            String path = configURL.substring(CLASSPATH_PREFIX.length());
            return descriptor.getOwnerClass().getClassLoader().getResource(path);
        }
        return new URL(configURL);
    }

    @Override
    public void lock(E key) {
        readLock.lock();
        underlyingCache.acquireWriteLockOnKey(key);
    }

    @Override
    public void unlock(E key) {
        underlyingCache.releaseWriteLockOnKey(key);
        readLock.unlock();
    }

    @Override
    public Lock getLock() {
        return writeLock;
    }

    @Override
    public Object load(E key) {
        Element element = underlyingCache.get(key);
        if (element == null) {
            return UNDEFINED;
        }
        return element.getObjectValue();
    }

    @Override
    public void save(E key, F value) {
        underlyingCache.put(new Element(key, value));
    }

    @Override
    public void clear() {
        underlyingCache.removeAll();
    }

    @Override
    public int size() {
        return (int) underlyingCache.getStatistics().getMemoryStoreObjectCount();
    }

    @Override
    public String toString() {
        return underlyingCache.toString();
    }
}
