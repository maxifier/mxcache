/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.hashing;

import java.lang.annotation.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Documented
@Target (ElementType.PARAMETER)
@Retention (RetentionPolicy.RUNTIME)
public @interface HashingStrategy {
    Class value();
}
