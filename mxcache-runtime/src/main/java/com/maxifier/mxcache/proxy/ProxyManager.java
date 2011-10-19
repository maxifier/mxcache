package com.maxifier.mxcache.proxy;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.NoSuchInstanceException;
import com.maxifier.mxcache.context.CacheContext;
import gnu.trove.THashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 25.10.2010
 * Time: 13:51:15
 */
public final class ProxyManager {
    private static final Logger logger = LoggerFactory.getLogger(ProxyManager.class);

    private ProxyManager() {
    }

    private static final ProxyManager INSTANCE = new ProxyManager();

    public static ProxyManager getInstance() {
        return INSTANCE;
    }

    private final Map<ProxyMappingKey, Class<? extends ProxyFactory>> mapping = new THashMap<ProxyMappingKey, Class<? extends ProxyFactory>>();

    @SuppressWarnings("UnusedDeclaration")
    // this method is used in generated code
    public ProxyFactory getProxyFactory(CacheContext context, Class owner, String name, String desc) {
        ProxyMappingKey key = new ProxyMappingKey(owner, name, desc);
        return getProxyFactory(context, key);
    }

    @SuppressWarnings("UnusedDeclaration")
    // this method is used in generated code by 2.1.9 instrumentator
    @Deprecated
    public ProxyFactory getProxyFactory(Class owner, String name, String desc) {
        return getProxyFactory(CacheFactory.getDefaultContext(), owner, name, desc);
    }

    private ProxyFactory getProxyFactory(CacheContext context, ProxyMappingKey key) {
        Class<? extends ProxyFactory> proxyFactoryClass = getProxyFactoryClass(key);
        if (proxyFactoryClass != null) {
            try {
                return context.getInstanceProvider().forClass(proxyFactoryClass);
            } catch (NoSuchInstanceException e) {
                logger.warn("Cannot create proxy factory for " + key, e);
            }
        }
        return null;
    }

    private Class<? extends ProxyFactory> getProxyFactoryClass(ProxyMappingKey key) {
        synchronized(mapping) {
            if (mapping.containsKey(key)) {
                return mapping.get(key);
            }
            Class<? extends ProxyFactory> proxyFactoryClass;
            UseProxy useProxy = key.getMethod().getAnnotation(UseProxy.class);
            if (useProxy == null) {
                logger.warn("Cannot create proxy factory for " + key + ": no @UseProxy");
                return null;
            } else {
                proxyFactoryClass = useProxy.value();
            }
            mapping.put(key, proxyFactoryClass);
            return proxyFactoryClass;
        }
    }
}
