/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import com.maxifier.mxcache.impl.instanceprovider.DefaultInstanceProvider;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
class DefaultCacheContext extends AbstractCacheContext {
    @Override
    public InstanceProvider getInstanceProvider() {
        return DefaultInstanceProvider.getInstance();
    }

    @Override
    public String toString() {
        return "DefaultCacheContext";
    }

}
