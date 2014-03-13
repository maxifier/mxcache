/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.guice;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.hashing.DefaultHashingStrategyFactory;
import com.maxifier.mxcache.transform.TransformGeneratorFactory;
import com.maxifier.mxcache.transform.TransformGeneratorFactoryImpl;
import com.maxifier.mxcache.clean.CacheCleaner;
import com.maxifier.mxcache.config.MxCacheConfigProvider;
import com.maxifier.mxcache.impl.DefaultStrategy;

/**
 * MxCacheGuiceModule
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class MxCacheGuiceModule extends MxCacheGuiceChildModule {
    public MxCacheGuiceModule() {
        super(CacheContext.class, "<root guice context>");
    }

    public MxCacheGuiceModule(String name) {
        super(CacheContext.class, name);
    }

    @Override
    protected void configure() {
        super.configure();

        // all standard classes should be placed here to prevent multiple instance creation
        // bind(<class>).toInstance()
        bind(DefaultStrategy.class).toInstance(DefaultStrategy.getInstance());
        bind(CacheCleaner.class).toInstance(CacheFactory.getCleaner());
        bind(MxCacheConfigProvider.class).toInstance(CacheFactory.getConfiguration());
        bind(TransformGeneratorFactory.class).toInstance(TransformGeneratorFactoryImpl.getInstance());
        bind(DefaultHashingStrategyFactory.class).toInstance(DefaultHashingStrategyFactory.getInstance());
    }
}
