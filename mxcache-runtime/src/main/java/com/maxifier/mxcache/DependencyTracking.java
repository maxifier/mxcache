/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

/**
 * Dependency tracking type
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public enum DependencyTracking {
    /**
     * Use inherited value (e.g. taken from parent or from defaults)
     */
    DEFAULT,
    /** No dependency tracking */
    NONE,
    /**
     * Caches of all instances of this cache will be cleaned at once.
     * <p>
     * It's much cheaper to have static dependency tracking.
     * If there are a lot of instances with cache and refresh takes quick consider using STATIC tracking
     * to improve performance.
     */
    STATIC,
    /**
     * Each instance has it's own dependency tracking, if a single cache is marked as dirty only this cache
     * will be cleaned.
     */
    INSTANCE
}
