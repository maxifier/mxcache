/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.config;

import com.maxifier.mxcache.DependencyTracking;
import com.maxifier.mxcache.AnnotatedDependencyTracking;
import com.maxifier.mxcache.provider.CachingStrategy;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface Rule {
    boolean getDisableCache();

    Object getProperty(String name);

    boolean isImportantProperty(String name);

    DependencyTracking getTrackDependency();

    AnnotatedDependencyTracking getTrackAnnotatedDependency();

    Set<String> getResourceDependencies();

    Set<String> getRuleNames();

    String getCacheName();

    Class<? extends CachingStrategy> getStrategy();

    void override(Method method, String cacheName);
}
