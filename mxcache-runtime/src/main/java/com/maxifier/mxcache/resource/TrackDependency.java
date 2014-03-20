/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.resource;

import com.maxifier.mxcache.DependencyTracking;

import java.lang.annotation.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target (ElementType.METHOD)
public @interface TrackDependency {
    DependencyTracking value() default DependencyTracking.STATIC;
}
