/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.resource;

import com.maxifier.mxcache.MxCacheException;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ResourceModificationException extends MxCacheException {
    public ResourceModificationException(String message) {
        super(message);
    }
}
