package com.maxifier.mxcache.config;

import com.maxifier.mxcache.DependencyTracking;
import com.maxifier.mxcache.provider.CachingStrategy;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 16.08.2010
 * Time: 14:58:43
 */
public interface Rule {
    boolean getDisableCache();

    Object getProperty(String name);

    boolean isImportantProperty(String name);

    DependencyTracking getTrackDependency();

    Set<String> getResourceDependencies();

    Set<String> getRuleNames();

    String getCacheName();

    Class<? extends CachingStrategy> getStrategy();

    void override(Method method, String cacheName);
}
