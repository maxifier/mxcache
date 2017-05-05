/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.instanceprovider;

import com.maxifier.mxcache.InstanceProvider;

import javax.annotation.Nonnull;

import java.util.Map;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
class BinderImpl<T> implements Binder<T> {
    private final Class<T> cls;
    private final InstanceProvider instanceProvider;
    private final Map<Class, Provider> registry;

    public BinderImpl(InstanceProvider instanceProvider, Map<Class, Provider> registry, Class<T> cls) {
        this.instanceProvider = instanceProvider;
        this.registry = registry;
        this.cls = cls;
    }

    @Override
    public void toProvider(@Nonnull Provider<T> provider) {
        registry.put(cls, provider);
    }

    @Override
    public void toInstance(@Nonnull T instance) {
        if (!cls.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot bind " + cls + " to instance of " + instance.getClass());
        }
        registry.put(cls, new ConstProvider<T>(instance));
    }

    @Override
    public void toClass(@Nonnull Class<? extends T> cls) {
        if (cls == this.cls) {
            throw new IllegalArgumentException("Cannot bind " + this.cls + " to itself");
        }
        if (!this.cls.isAssignableFrom(cls)) {
            throw new IllegalArgumentException("Cannot bind " + this.cls + " to " + cls);
        }
        registry.put(this.cls, new DelegatingProvider<T>(instanceProvider, cls));
    }
}
