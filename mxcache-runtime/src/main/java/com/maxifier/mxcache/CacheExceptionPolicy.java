/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation allows to clarify the policy of handling exceptions in cached methods.
 * By default all exceptions are rethrown immediately. This annotation allows to override
 * the default settings.
 *
 * It also allows to specify special cases for certain exception types via {@link #specialCases()}.
 *
 * Please note: caches never handle Errors.
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheExceptionPolicy {
    /**
     * Controls whether the exceptions will be stored in cache.
     *
     * If set to true, the cache will remember that a certain key caused an exception
     * and will throw the same exception on the next invocation of cached method with
     * the same key.
     *
     * Regardless of whether this flag is set or not, in case of exception the cache
     * will recall original method for {@link #retries()} times. If any of these calls
     * succeeds, the value will be saved to cache and returned.
     *
     * The exception is stored for {@link #rememberExceptionTimeoutMillis()} milliseconds.
     * After that the original method will be invoked again.
     *
     * This setting may be overriden for specific exception type with {@link #specialCases()}.
     *
     * @return true means remember exceptions; false means rethrow it immediately
     */
    boolean rememberExceptions() default false;

    /**
     * In case of exception the cache will recall original method for specified number of times.
     * If any of these calls succeeds, the value will be saved to cache and returned.
     *
     * The value of 0 (by default) means that if the first invocation of original method throws
     * an exception, the cache will return it to the client without retrying.
     *
     * Before each retry, the cache will wait for {@link #sleepBeforeRetryMillis()} ms.
     *
     * This setting may be overriden for specific exception type with {@link #specialCases()}.
     *
     * @return the number of retries
     */
    int retries() default 0;

    /**
     * If {@link #retries()} is non-zero, the cache will wait for specified number of milliseconds
     * before each retry. Ignored otherwise.
     *
     * This setting may be overriden for specific exception type with {@link #specialCases()}.
     *
     * @return the number of millis to wait before each retry
     */
    long sleepBeforeRetryMillis() default 0;

    /**
     * If {@link #rememberExceptions} is set to true, this property specifies the number of
     * milliseconds for which the exception will be stored in cache. Ignored otherwise.
     *
     * This setting may be overriden for specific exception type with {@link #specialCases()}.
     *
     * Zero timeout means that the exception will be stored forever.
     *
     * @return how many milliseconds the exception will be stored
     */
    long rememberExceptionTimeoutMillis() default 0;

    /**
     * You can override the properties for exception handling for certain exception types.
     * All special cases are evaluated sequentially, in the same way as "catch" clauses.
     *
     * @return special cases for exception handling
     */
    SpecialCase[] specialCases() default {};

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SpecialCase {
        /**
         * @return the exception type to handle
         */
        Class<? extends Exception> exceptionClass();

        /**
         * Overrides {@link CacheExceptionPolicy#rememberExceptions()}.
         *
         * There is no default value for this one because for other properties default value  '-1' means
         * 'inherit from enclosing CacheExceptionPolicy', but there's no special value for boolean that can
         * be used in this case.
         *
         * @return true means remember exceptions; false means rethrow it immediately
         */
        boolean rememberExceptions();

        /**
         * Overrides {@link CacheExceptionPolicy#retries()}.
         *
         * Default value '-1' means 'inherit from enclosing CacheExceptionPolicy'.
         *
         * @return the number of retries
         */
        int retries() default -1;

        /**
         * Overrides {@link CacheExceptionPolicy#sleepBeforeRetryMillis()}.
         *
         * Default value '-1' means 'inherit from enclosing CacheExceptionPolicy'.
         *
         * @return the number of millis to wait before each retry
         */
        long sleepBeforeRetryMillis() default -1;

        /**
         * Overrides {@link CacheExceptionPolicy#rememberExceptionTimeoutMillis()}.
         *
         * Default value '-1' means 'inherit from enclosing CacheExceptionPolicy'.
         *
         * Zero timeout means that the exception will be stored forever.
         *
         * @return how many milliseconds the exception will be stored
         */
        long rememberExceptionTimeoutMillis() default -1;
    }
}
