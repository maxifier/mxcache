/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.config;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class TestMxCacheConfigProvider extends MxCacheConfigProviderImpl {
    private final MxCacheConfig bootstrapConfig;

    public TestMxCacheConfigProvider(MxCacheConfig bootstrapConfig) {
        super(false);
        this.bootstrapConfig = bootstrapConfig;
    }

    @Override
    MxCacheConfig loadBootstrapConfig() {
        return bootstrapConfig;
    }
}
