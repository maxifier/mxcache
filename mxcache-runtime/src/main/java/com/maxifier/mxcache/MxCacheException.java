/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class MxCacheException extends RuntimeException {
    public MxCacheException() {
    }

    public MxCacheException(String message) {
        super(message);
    }

    public MxCacheException(String message, Throwable cause) {
        super(message, cause);
    }

    public MxCacheException(Throwable cause) {
        super(cause);
    }
}
