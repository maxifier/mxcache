/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.MxCacheException;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
public class InvalidTransformAnnotations extends MxCacheException {
    public InvalidTransformAnnotations(String message) {
        super(message);
    }

    public InvalidTransformAnnotations(String message, Throwable cause) {
        super(message, cause);
    }
}
