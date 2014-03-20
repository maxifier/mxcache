/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.guice;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.maxifier.mxcache.AbstractCacheContext;
import com.maxifier.mxcache.InstanceProvider;

import javax.annotation.Nonnull;

/**
 * GuiceInstanceProvider
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Singleton
public class GuiceInstanceProvider extends AbstractCacheContext implements InstanceProvider {
    private Injector injector;
    private String name;

    @Inject(optional = true)
    public void setName(@GuiceInstanceProviderName String name) {
        this.name = name;
    }

    @Inject
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    @Nonnull
    @Override
    public <T> T forClass(@Nonnull Class<T> cls) {
        return injector.getInstance(cls);
    }

    @Override
    public InstanceProvider getInstanceProvider() {
        return this;
    }

    @Override
    public String toString() {
        return name == null ? super.toString() : name;
    }
}
