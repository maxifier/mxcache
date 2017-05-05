/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import com.maxifier.mxcache.provider.CachingStrategy;

import java.lang.annotation.*;

/**
 * This annotation allows you to add your own caching strategy to a cached method.
 * @see com.maxifier.mxcache.UseStorage
 * @see com.maxifier.mxcache.UseStorageFactory
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Documented
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.METHOD)
public @interface Strategy {
    Class<? extends CachingStrategy> value();
}
