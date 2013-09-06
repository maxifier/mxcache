package com.maxifier.mxcache.proxy;

import gnu.trove.THashMap;

import java.util.Map;

public final class MxProxyGenerator {
    private static final Map<MxProxyFactory, MxProxyFactory> PROXY_MAP = new THashMap<MxProxyFactory, MxProxyFactory>();
    private static final Map<MxGenericProxyFactory, MxGenericProxyFactory> GENERIC_PROXY_MAP = new THashMap<MxGenericProxyFactory, MxGenericProxyFactory>();

    private MxProxyGenerator() {
    }

    /**
     * Создает фабрику проксей заданного типа.
     *
     * @param sourceClass    исходный интерфейс
     * @param containerClass класс/интерфейс контейнера
     * @return фабрику проксей для заданного типа объектов и контейнеров.
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
     * Создает generic фабрику проксей заданного типа.
     * Отличасется тем, что возвращаемые прокси помимо самого исходного интерфейса реализуют также все его
     * интерфейсы-наследники, которые реализует исходный объект
     *
     * @param sourceClass    исходный интерфейс
     * @param containerClass класс/интерфейс контейнера
     * @return фабрику проксей для заданного типа объектов и контейнеров.
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
