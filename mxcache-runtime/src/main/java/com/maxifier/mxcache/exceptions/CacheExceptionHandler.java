/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.exceptions;

import com.maxifier.mxcache.CacheExceptionPolicy;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class CacheExceptionHandler {
    private final CacheExceptionPolicy policy;

    public CacheExceptionHandler(CacheExceptionPolicy policy) {
        this.policy = policy;
    }

    /**
     * Computes the action for exception. Sleeps if necessary.
     *
     * @param retry the number of retries already made. 0 means that this is the original invocation failed
     * @param e exception
     */
    public Action getAction(int retry, Exception e) {
        if (policy == null) {
            return Action.RETHROW;
        }

        for (CacheExceptionPolicy.SpecialCase specialCase : policy.specialCases()) {
            if (specialCase.exceptionClass().isAssignableFrom(e.getClass())) {
                int retries = specialCase.retries();
                if (retries < 0) {
                    retries = policy.retries();
                }
                if (retry < retries) {
                    long sleep = specialCase.sleepBeforeRetryMillis();
                    if (sleep < 0) {
                        sleep = policy.sleepBeforeRetryMillis();
                    }
                    if (sleep > 0) {
                        try {
                            Thread.sleep(sleep);
                        } catch (InterruptedException interrupted) {
                            // we will immediately rethrow an exception on interruption to prevent hanging
                            Thread.interrupted();
                            return Action.RETHROW;
                        }
                    }
                    return Action.RETRY;
                }
                return specialCase.rememberExceptions() ? Action.REMEMBER_AND_RETHROW : Action.RETHROW;
            }
        }
        if (retry < policy.retries()) {
            long sleep = policy.sleepBeforeRetryMillis();
            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException interrupted) {
                    // we will immediately rethrow an exception on interruption to prevent hanging
                    Thread.interrupted();
                    return Action.RETHROW;
                }
            }
            return Action.RETRY;
        }
        return policy.rememberExceptions() ? Action.REMEMBER_AND_RETHROW : Action.RETHROW;
    }

    /**
     * Computes the timestamp of expiration of given exception
     * @param e exception
     * @return timeout in millis
     */
    public long getRememberExceptionExpirationTimestamp(Exception e) {
        for (CacheExceptionPolicy.SpecialCase specialCase : policy.specialCases()) {
            if (specialCase.exceptionClass().isAssignableFrom(e.getClass())) {
                long timeout = specialCase.rememberExceptionTimeoutMillis();
                if (timeout < 0) {
                    break;
                }
                if (timeout == 0) {
                    // unlimited timeout
                    return 0;
                }
                return timeout + System.currentTimeMillis();
            }
        }
        long timeout = policy.rememberExceptionTimeoutMillis();
        if (timeout == 0) {
            return 0;
        }
        return timeout + System.currentTimeMillis();
    }

    public enum Action {
        RETRY,
        REMEMBER_AND_RETHROW,
        RETHROW
    }
}
