/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import java.lang.annotation.*;

/**
 * Add this annotation to your @Cached method to identify how statistics for it should be gathered.
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Documented
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.METHOD)
public @interface StatisticsMode {
    StatisticsModeEnum value();
}
