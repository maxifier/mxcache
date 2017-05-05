/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.resource;

import com.maxifier.mxcache.DependencyTracking;

import java.lang.annotation.*;

/**
 * Use this annotation among with @Cached to specify dependency tracking strategy.
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target (ElementType.METHOD)
public @interface TrackDependency {
    DependencyTracking value() default DependencyTracking.STATIC;
}
