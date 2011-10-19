package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.proxy.ProxyFactory;
import com.maxifier.mxcache.proxy.Resolvable;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 19.10.2010
 * Time: 14:55:51
 */
public class TestProxyFactory implements ProxyFactory<String> {
    int call;

    @Override
    public String proxy(Class<String> c, Resolvable<String> value) {
        return value.getValue() + call++;
    }
}
