/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.context;

import com.maxifier.mxcache.AbstractCacheContext;
import com.maxifier.mxcache.InstanceProvider;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class CacheContextImpl extends AbstractCacheContext {
    private final InstanceProvider instanceProvider;

    public CacheContextImpl(InstanceProvider instanceProvider) {
        this.instanceProvider = instanceProvider;
    }

    public InstanceProvider getInstanceProvider() {
        return instanceProvider;
    }

    @Override
    public String toString() {
        return "ContextImpl{" + instanceProvider + "}";
    }
}
