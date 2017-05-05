/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class NoSuchInstanceException extends MxCacheException {
    public NoSuchInstanceException() {
    }

    public NoSuchInstanceException(String message) {
        super(message);
    }

    public NoSuchInstanceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchInstanceException(Throwable cause) {
        super(cause);
    }
}
