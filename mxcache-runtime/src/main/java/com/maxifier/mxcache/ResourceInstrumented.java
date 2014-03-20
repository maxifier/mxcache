/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import java.lang.annotation.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 * <p>
 * This annotation is added to all instrumented classes with @Cached methods
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
