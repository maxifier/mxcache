/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.proxy;

import java.lang.ref.WeakReference;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
public class WeakProxyFactory<Value> implements ProxyFactory<Value> {
    public static class WeakResolvable<T> implements Resolvable<T> {
        private static final Object NON_INITIALIZED = new Object();

        private final Resolvable<T> resolvable;
        private WeakReference<T> cached;

        public WeakResolvable(Resolvable<T> resolvable) {
            this.resolvable = resolvable;
        }

        @Override
        public T getValue() {
            Object value = load();
            if (value == NON_INITIALIZED) {
                T t = resolvable.getValue();
                save(t);
                return t;
            }
            //noinspection unchecked
            return (T)value;
        }

        private synchronized Object load() {
            if (cached == null) {
                return NON_INITIALIZED;
            }
            T v = cached.get();
            if (v == null) {
                return NON_INITIALIZED;
            }
            return v;
        }

        private synchronized void save(T value) {
            cached = new WeakReference<T>(value);
        }
    }

    @Override
    public Value proxy(Class<Value> expected, Resolvable<Value> resolvable) {
        //noinspection RedundantCast
        return (Value) MxProxyGenerator.getProxyFactory(expected, WeakResolvable.class).createProxy(new WeakResolvable<Value>(resolvable));
    }
}
