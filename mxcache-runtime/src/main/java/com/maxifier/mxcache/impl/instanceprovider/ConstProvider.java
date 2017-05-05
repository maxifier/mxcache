/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.instanceprovider;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
class ConstProvider<T> implements Provider {
    private final T instance;

    public ConstProvider(T instance) {
        this.instance = instance;
    }

    @Override
    public Object get() {
        return instance;
    }
}
