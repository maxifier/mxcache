/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.METHOD;

/**
 * Cached - this annotation tells MxCache instrumentator that method marked with this annotation should be instrumented.
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {METHOD})
@Documented
public @interface Cached {
    /**
     * @return Cache group name. It's just another way of cleaning caches.
     */
    String group() default "";

    /**
     * @return name of cache. Can be used by strategies to identify cache (e.g. ehcache). Displayed in JConsole plugin.
     */
    String name() default "";

    /**
     * Tag is any string. They are used to clear caches.
     * @see CacheFactory#getCleaner()
     * @return tags of cache.
     */
    String[] tags() default {};

    /**
     * Not used actually
     * @return activity; ignored
     */
    String activity() default "";
}
