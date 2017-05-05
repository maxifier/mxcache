/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.proxy;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ConstResolvable<T> implements Resolvable<T> {
    private final T value;

    public ConstResolvable(T value) {
        this.value = value;
    }

    @Override
    public T getValue() {
        return value;
    }
}
