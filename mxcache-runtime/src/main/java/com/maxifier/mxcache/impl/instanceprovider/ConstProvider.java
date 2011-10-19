package com.maxifier.mxcache.impl.instanceprovider;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 15.09.2010
 * Time: 9:15:02
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
