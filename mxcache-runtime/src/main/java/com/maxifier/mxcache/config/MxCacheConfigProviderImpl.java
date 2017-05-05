/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.util.*;

import static com.maxifier.mxcache.impl.CacheProviderImpl.registerMBean;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class MxCacheConfigProviderImpl implements MxCacheConfigProvider {
    private static final Logger logger = LoggerFactory.getLogger(MxCacheConfigProviderImpl.class);

    private final Map<ClassLoader, MxCacheConfig> configs = new WeakHashMap<ClassLoader, MxCacheConfig>();

    public MxCacheConfigProviderImpl(boolean needsMBean) {
        if (needsMBean) {
            registerMBean(new ConfigurationControl(this), "com.maxifier.mxcache:service=ConfigurationControl");
        }
    }

    @Override
    public Rule getRule(Class cls, String group, String[] tags) {
        JaxbRule result = new JaxbRule();
        for (RuleWithSelector rule : getConfig(cls.getClassLoader()).getRules()) {
            if (rule.matches(cls, group, tags)) {
                result.addRuleName(rule.getName());
                result.override(rule);
            }
        }
        return result;
    }

    @Override
    public List<ResourceConfig> getResources() {
        return getConfig(null).getResources();
    }

    public List<RuleWithSelector> getRules() {
        Set<RuleWithSelector> res = new LinkedHashSet<RuleWithSelector>();
        for (MxCacheConfig config : configs.values()) {
            res.addAll(config.getRules());
        }
        return new ArrayList<RuleWithSelector>(res);
    }

    // may be overriden in tests
    MxCacheConfig loadBootstrapConfig() {
        MxCacheConfig config = new MxCacheConfig();
        try {
            JAXBContext context = getJAXBContext();
            Enumeration<URL> configs = ClassLoader.getSystemResources("META-INF/mxcache.xml");
            while (configs.hasMoreElements()) {
                overrideFrom(config, context, configs.nextElement());
            }
            String path = System.getProperty("mxcache.xml.path");
            if (path != null) {
                overrideFrom(config, context, new File(path));
            }
            String url = System.getProperty("mxcache.xml.url");
            if (url != null) {
                overrideFrom(config, context, new URL(url));
            }
            File f = new File("mxcache.xml");
            if (f.exists()) {
                overrideFrom(config, context, f);
            }
        } catch (Exception e) {
            logLoadingError(e, "unknown");
        }
        return config;
    }

    private MxCacheConfig loadConfig(ClassLoader classLoader) {
        if (classLoader == null) {
            return loadBootstrapConfig();
        }
        MxCacheConfig parentConfig = getConfig(classLoader.getParent());
        MxCacheConfig config = new MxCacheConfig();
        config.merge(parentConfig);
        try {
            JAXBContext context = getJAXBContext();
            Enumeration<URL> configs = classLoader.getResources("META-INF/mxcache.xml");
            while (configs.hasMoreElements()) {
                URL url = configs.nextElement();
                if (!parentConfig.hasSource(url)) {
                    overrideFrom(config, context, url);
                }
            }
        } catch (Exception e) {
            logLoadingError(e, "unknown");
        }
        return config;
    }

    private synchronized MxCacheConfig getConfig(ClassLoader classLoader) {
        MxCacheConfig config = configs.get(classLoader);
        if (config == null) {
            config = loadConfig(classLoader);
            configs.put(classLoader, config);
        }
        return config;
    }

    private static void overrideFrom(MxCacheConfig config, JAXBContext context, File file) {
        try {
            MxCacheConfig newConfig = load(context, file);
            newConfig.setSource(file.toString());
            config.merge(newConfig);
        } catch (Exception e) {
            logLoadingError(e, file.toString());
        }
    }

    private static void overrideFrom(MxCacheConfig config, JAXBContext context, URL url) {
        try {
            MxCacheConfig newConfig = load(context, url);
            newConfig.setSource(url.toString());
            config.merge(newConfig);
        } catch (Exception e) {
            logLoadingError(e, url.toString());
        }
    }

    private static void logLoadingError(Exception e, String source) {
        // we don't throw exceptions cause it will cause fatal error and MxCache couldn't be loaded
        logger.error("MxCache configuration loading failed (" + source + ")", e);
    }

    static MxCacheConfig load(JAXBContext context, URL url) throws JAXBException {
        MxCacheConfig res = (MxCacheConfig) context.createUnmarshaller().unmarshal(url);
        res.addSource(url);
        return res;
    }

    static MxCacheConfig load(JAXBContext context, File path) throws JAXBException {
        return (MxCacheConfig) context.createUnmarshaller().unmarshal(path);
    }

    static JAXBContext getJAXBContext() throws JAXBException {
        return JAXBContext.newInstance(MxCacheConfig.class.getPackage().getName());
    }

    public static JaxbRule loadRule(String... xmlRules) throws JAXBException {
        JAXBContext context = getJAXBContext();
        JaxbRule r = new JaxbRule();
        for (String rule : xmlRules) {
            r.override((JaxbRule) context.createUnmarshaller().unmarshal(new StringReader(rule)));
        }
        return r;
    }
}
