/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.clean;

import com.maxifier.mxcache.PublicAPI;
import com.maxifier.mxcache.caches.CleaningNode;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface CacheCleaner {
    /**
     * Clears all caches in given instance.
     * Doesn't clean static caches.
     */
    @PublicAPI
    void clearCacheByInstance(Object o);

    /**
     * Clears all caches in given instances.
     * Doesn't clean static caches.
     */
    @PublicAPI
    void clearCacheByInstances(Object... o);

    /**
     * Clears all caches in given instance having given tag.
     * Doesn't clean static caches.
     */
    @PublicAPI
    void clearInstanceByTag(Object o, String tag);

    /**
     * Clears all caches in given instance having given group.
     * Doesn't clean static caches.
     */
    @PublicAPI
    void clearInstanceByGroup(Object o, String group);

    /**
     * Clears all non-static caches of given class, including the ones inherited from parent class or declared in
     * ancestors.
     * Clears static classes of this class and it's ancestors (leaves parent class static caches untouched).
     */
    @PublicAPI
    void clearCacheByClass(Class<?> aClass);

    /**
     * Clears all caches having given group.
     */
    @PublicAPI
    void clearCacheByGroup(String group);

    /**
     * Clears all caches having given tag.
     */
    @PublicAPI
    void clearCacheByTag(String tag);

    /**
     * Clears all caches having given annotation class
     */
    @PublicAPI
    void clearCacheByAnnotation(Class<? extends Annotation> annotationClass);

    /**
     * Clears all passed cleaning nodes
     */
    @PublicAPI
    void clearAll(Collection<? extends CleaningNode> elements);
}
