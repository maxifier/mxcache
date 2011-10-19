package com.maxifier.mxcache.ehcache;

import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.StorageFactory;
import com.maxifier.mxcache.storage.Storage;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 14.03.11
* Time: 18:33
*/
public class EhcacheStorageFactory<T> implements StorageFactory<T> {
    private static final Logger logger = LoggerFactory.getLogger(EhcacheStorageFactory.class);

    private final CacheManager cacheManager;
    private final CacheConfiguration configuration;

    public EhcacheStorageFactory(CacheDescriptor<T> descriptor, CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        configuration = ConfigurationParser.parseConfiguration(descriptor);
    }

    @Override
    public Storage createStorage(T owner) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        String name = configuration.getName();
        String tempName = findFreeName(name);
        configuration.setName(tempName);
        Cache cache = new Cache(configuration);
        cacheManager.addCache(cache);
        return new EhcacheStorage(cache);
    }

    private String findFreeName(String name) {
        if (cacheManager.getCache(name) == null) {
            return name;
        }
        String newName;
        int i = 2;
        do {
            newName = name + "$" + i++;
        } while (cacheManager.getCache(newName) != null);
        logger.warn("Multiple ehcache with name <" + name + ">, will use <" + newName + "> instead");
        return newName;
    }

    @Override
    public String getImplementationDetails() {
        return "EhCache:" + configuration;
    }
}
