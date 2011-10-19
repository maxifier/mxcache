package com.maxifier.mxcache.impl.instanceprovider;

import com.maxifier.mxcache.InstanceProvider;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 18.10.2010
* Time: 11:31:39
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
