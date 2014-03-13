/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.resource.MxResource;

import javax.annotation.Nonnull;

/**
 * ResourceOccupied.
 *
 * Please don't catch this Error. It is a part of dependency tracking and deadlock prevention of MxCache.
 *
 * <b>It doesn't fill the stack trace!!!</b>
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ResourceOccupied extends Error {
    private final MxResource resource;

    public ResourceOccupied(@Nonnull MxResource resource) {
        this.resource = resource;
    }

    public MxResource getResource() {
        return resource;
    }

    @Override
    public Throwable fillInStackTrace() {
        // do nothing - we only need to traverse stack, not stacktrace.
        return this;
    }

    @Override
    public String getMessage() {
        return "Resource \"" + resource + "\" is locked for write. Stack will be unrolled and topmost cached method will wait for resource to be released. This exception should not be caught";
    }
}
