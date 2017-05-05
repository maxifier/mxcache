/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.instanceprovider;

import com.maxifier.mxcache.InstanceProvider;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
class DelegatingProvider<T> implements Provider<T> {
    private final InstanceProvider instanceProvider;
    private final Class<? extends T> cls;

    public DelegatingProvider(InstanceProvider instanceProvider, Class<? extends T> cls) {
        this.instanceProvider = instanceProvider;
        this.cls = cls;
    }

    @Override
    public T get() {
        return instanceProvider.forClass(cls);
    }
}
