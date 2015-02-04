/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.exceptions;

/**
 * If exception handling policy allows remembering exceptions on cached methods, the exception will be wrapped
 * into this object and storage in cache as usual.
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class ExceptionRecord {
    private final Exception exception;
    private final long expirationTime;

    public ExceptionRecord(Exception exception, long expirationTime) {
        this.exception = exception;
        this.expirationTime = expirationTime;
    }

    public Exception getException() {
        return exception;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    @Override
    public String toString() {
        return "ExceptionRecord{" +
                "exception=" + exception +
                ", expirationTime=" + expirationTime +
                '}';
    }
}
