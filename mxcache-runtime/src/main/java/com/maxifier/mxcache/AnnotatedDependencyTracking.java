/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

/**
 * Dependency tracking annotations using
 *
 * @author Aleksey Tomin (aleksey.tomin@cxense.com)
 */
public enum AnnotatedDependencyTracking {
    /**
     * Use inherited value (e.g. taken from parent or from defaults)
     */
    DEFAULT,
    /**
     * Add DependencyTracking by all annotations
     */
    ALL,
    /**
     * Ignoryng all annotations
     */
    NONE
}
