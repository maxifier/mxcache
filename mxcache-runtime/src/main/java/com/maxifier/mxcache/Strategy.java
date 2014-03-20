/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import com.maxifier.mxcache.provider.CachingStrategy;

import java.lang.annotation.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Documented
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.METHOD)
public @interface Strategy {
    Class<? extends CachingStrategy> value();
}
