/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.proxy;

import gnu.trove.THashMap;

import java.util.Map;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class MxProxyGenerator {
    private static final Map<MxProxyFactory, MxProxyFactory> PROXY_MAP = new THashMap<MxProxyFactory, MxProxyFactory>();
    private static final Map<MxGenericProxyFactory, MxGenericProxyFactory> GENERIC_PROXY_MAP = new THashMap<MxGenericProxyFactory, MxGenericProxyFactory>();

    private MxProxyGenerator() {
    }

    /**
     * Creates a proxy factory for given interface.
     *
     * @param sourceClass    source interface
     * @param containerClass container type interface
     * @return created factory
     */
    public static synchronized <T, C extends Resolvable<T>> MxProxyFactory<T, C> getProxyFactory(Class<T> sourceClass, Class<C> containerClass) {
        MxProxyFactory<T, C> proxy = new MxProxyFactory<T, C>(sourceClass, containerClass);
        @SuppressWarnings({"unchecked"})
        MxProxyFactory<T, C> oldProxy = PROXY_MAP.get(proxy);
        if (oldProxy == null) {
            PROXY_MAP.put(proxy, proxy);
            return proxy;
        }
        return oldProxy;
    }

    /**
     * Creates generic proxy factory of given type.
     * The difference between usual and generic proxy factory is that proxies created by generic proxy will implement
     * not only target interface but also all its subinterfaces that initial object has.
     *
     * @param sourceClass    source interface
     * @param containerClass container type interface
     * @return created factory
     */
    public static synchronized <T, C extends Resolvable<T>> MxGenericProxyFactory<T, C> getGenericProxyFactory(Class<T> sourceClass, Class<C> containerClass) {
        MxGenericProxyFactory<T, C> proxy = new MxGenericProxyFactory<T, C>(sourceClass, containerClass);
        @SuppressWarnings({"unchecked"})
        MxGenericProxyFactory<T, C> oldProxy = GENERIC_PROXY_MAP.get(proxy);
        if (oldProxy == null) {
            GENERIC_PROXY_MAP.put(proxy, proxy);
            return proxy;
        }
        return oldProxy;
    }
}
