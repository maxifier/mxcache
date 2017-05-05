/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.proxy.ProxyFactory;
import com.maxifier.mxcache.proxy.Resolvable;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
public class TestProxyFactoryUninstantiatable implements ProxyFactory {
    public TestProxyFactoryUninstantiatable(String youCannotInstanceMe) {
    }

    @Override
    public Object proxy(Class c, Resolvable value) {
        return value;
    }
}
