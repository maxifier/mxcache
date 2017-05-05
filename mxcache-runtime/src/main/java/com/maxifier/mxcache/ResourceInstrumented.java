/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import java.lang.annotation.*;

/**
 * This annotation is added to all instrumented classes with @Cached methods
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ResourceInstrumented {
    /**
     * @return version of instrumentator
     */
    String version();

    /**
     * @return minimal compatible version of runtime
     */
    String compatibleVersion();
}
