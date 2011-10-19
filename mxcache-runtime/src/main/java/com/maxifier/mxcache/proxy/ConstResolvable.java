package com.maxifier.mxcache.proxy;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 08.11.2010
 * Time: 15:50:53
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
