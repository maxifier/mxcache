package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.proxy.ProxyFactory;
import com.maxifier.mxcache.proxy.Resolvable;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 25.10.2010
* Time: 11:10:06
*/
public class TestProxyFactoryUninstantiatable implements ProxyFactory {
    public TestProxyFactoryUninstantiatable(String youCannotInstanceMe) {
    }

    @Override
    public Object proxy(Class c, Resolvable value) {
        return value;
    }
}
