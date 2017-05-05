/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.storage;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.caches.Calculable;
import com.maxifier.mxcache.exceptions.ExceptionHelper;
import com.maxifier.mxcache.provider.Signature;
import com.maxifier.mxcache.storage.Storage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class TestHelper {
    private TestHelper() {
    }

    public static Object getOrCreate(Cache o, Object key) throws IllegalAccessException {
        Signature s = Signature.of(o.getClass());
        Method m = findMethod(s.getCacheInterface(), "getOrCreate");
        try {
            return s.getContainer() == null ? m.invoke(o) : m.invoke(o, key);
        } catch (InvocationTargetException e) {
            ExceptionHelper.throwCheckedExceptionHack(e.getTargetException());
            return null;
        }
    }

    public static Object load(Storage o, Object key) throws InvocationTargetException, IllegalAccessException {
        Signature s = Signature.of(o.getClass());
        Method m = findMethod(s.getStorageInterface(), "load");
        try {
            return s.getContainer() == null ? m.invoke(o) : m.invoke(o, key);
        } catch (InvocationTargetException e) {
            ExceptionHelper.throwCheckedExceptionHack(e.getTargetException());
            return null;
        }
    }

    public static void lock(Storage o, Object key) throws InvocationTargetException, IllegalAccessException {
        try {
            findMethod(Signature.of(o.getClass()).getElementLockedStorageInterface(), "lock").invoke(o, key);
        } catch (InvocationTargetException e) {
            ExceptionHelper.throwCheckedExceptionHack(e.getTargetException());
        }
    }

    public static void unlock(Storage o, Object key) throws InvocationTargetException, IllegalAccessException {
        try {
            findMethod(Signature.of(o.getClass()).getElementLockedStorageInterface(), "unlock").invoke(o, key);
        } catch (InvocationTargetException e) {
            ExceptionHelper.throwCheckedExceptionHack(e.getTargetException());
        }
    }

    public static Object calculate(Calculable o, Object key) throws InvocationTargetException, IllegalAccessException {
        return calculate(o, "123", key);
    }

    public static Object calculate(Calculable o, Object owner, Object key) throws InvocationTargetException, IllegalAccessException {
        Signature s = Signature.of(o.getClass());
        Method m = findMethod(s.getCalculableInterface(), "calculate");
        try {
            return s.getContainer() == null ? m.invoke(o, owner) : m.invoke(o, owner, key);
        } catch (InvocationTargetException e) {
            ExceptionHelper.throwCheckedExceptionHack(e.getTargetException());
            return null;
        }
    }

    public static void save(Storage o, Object key, Object value) throws InvocationTargetException, IllegalAccessException {
        Signature s = Signature.of(o.getClass());
        Method m = findMethod(s.getStorageInterface(), "save");
        try {
            if (s.getContainer() == null) {
                m.invoke(o, value);
            } else {
                m.invoke(o, key, value);
            }
        } catch (InvocationTargetException e) {
            ExceptionHelper.throwCheckedExceptionHack(e.getTargetException());
        }
    }

    private static Method findMethod(Class<?> cacheInterface, String name) {
        for (Method method : cacheInterface.getMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        throw new RuntimeException("Can't find getOrCreate in " + cacheInterface);
    }
}
