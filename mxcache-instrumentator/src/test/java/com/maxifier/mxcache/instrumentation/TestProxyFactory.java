/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.proxy.ProxyFactory;
import com.maxifier.mxcache.proxy.Resolvable;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class TestProxyFactory implements ProxyFactory<String> {
    int call;

    @Override
    public String proxy(Class<String> c, Resolvable<String> value) {
        return value.getValue() + call++;
    }
}
