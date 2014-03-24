/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.resource;

import java.lang.annotation.*;

/**
 * Use this annotation among with @Cached to make your cache be cleaned when certain resource is modified.
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceDependency {
    /**
     * @return names of resources
     */
    String[] value();
}