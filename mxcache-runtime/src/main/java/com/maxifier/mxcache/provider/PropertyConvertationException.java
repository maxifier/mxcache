/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.provider;

import com.maxifier.mxcache.MxCacheException;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class PropertyConvertationException extends MxCacheException {
    public PropertyConvertationException() {
    }

    public PropertyConvertationException(String message) {
        super(message);
    }

    public PropertyConvertationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PropertyConvertationException(Throwable cause) {
        super(cause);
    }
}
