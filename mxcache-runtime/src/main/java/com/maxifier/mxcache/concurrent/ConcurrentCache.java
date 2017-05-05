/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.concurrent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ConcurrentCache is used to set caching strategy to {@link java.util.concurrent.ConcurrentHashMap} backed
 * implementation.
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-09-06 15:24)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ConcurrentCache {
    int DEFAULT_CONCURRENCY_LEVEL = 4;
    int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * This property could be set alternatively in mxcache.xml configuration as "concurrency-level".
     *
     * @return concurrency level for underlying {@link java.util.concurrent.ConcurrentHashMap}.
     */
    int concurrencyLevel() default DEFAULT_CONCURRENCY_LEVEL;

    /**
     * This property could be set alternatively in mxcache.xml configuration as "initial-capacity".
     *
     * @return initial capacity for underlying {@link java.util.concurrent.ConcurrentHashMap}.
     */
    int initialCapacity() default DEFAULT_INITIAL_CAPACITY;
}

