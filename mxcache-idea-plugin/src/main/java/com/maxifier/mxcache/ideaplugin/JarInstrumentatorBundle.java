package com.maxifier.mxcache.ideaplugin;

import com.maxifier.mxcache.MxCache;
import com.maxifier.mxcache.instrumentation.Instrumentator;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 06.03.12
 * Time: 13:21
 */
public class JarInstrumentatorBundle implements InstrumentatorBundle {
    private final URL[] urls;
    
    private ClassLoader classLoader;

    public JarInstrumentatorBundle(URL[] urls) {
        this.urls = urls;
    }

    @Override
    public Map<String, Instrumentator> getAvailableVersions() {
        try {
            Class<?> cls = findClass(MxCache.class);
            Method method = cls.getDeclaredMethod("getVersionString");
            //noinspection unchecked
            return (Map<String, Instrumentator>)method.invoke(null);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    @Override
    public MxCache.Version getVersion() {
        try {
            Class<?> cls = findClass(MxCache.class);
            Method method = cls.getDeclaredMethod("getVersionString");
            return new MxCache.Version((String)method.invoke(null));
        } catch (Exception e) {
            return MxCache.Version.UNKNOWN;
        }
    }
    
    private Class<?> findClass(Class<?> source) throws ClassNotFoundException {
        ClassLoader cl = getClassLoader();
        return cl.loadClass(source.getName());
    }

    public synchronized ClassLoader getClassLoader() {
        if (classLoader == null) {
            classLoader = new URLClassLoader(urls, getClass().getClassLoader());
        }
        return classLoader;
    }
}
