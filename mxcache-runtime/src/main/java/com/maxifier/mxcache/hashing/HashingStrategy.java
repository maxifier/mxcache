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
// todo rename this class, its name clashes with Trove name
public @interface HashingStrategy {
    Class<? extends gnu.trove.strategy.HashingStrategy> value();
}
