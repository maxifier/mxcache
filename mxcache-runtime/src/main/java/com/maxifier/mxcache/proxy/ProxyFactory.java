package com.maxifier.mxcache.proxy;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 11.10.2010
* Time: 12:12:51
*/
public interface ProxyFactory<T> {
    T proxy(Class<T> expected, Resolvable<T> value);
}
