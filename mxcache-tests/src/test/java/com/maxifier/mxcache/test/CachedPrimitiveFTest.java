/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.test;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.Cached;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.testng.Assert.assertNull;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class CachedPrimitiveFTest {
    static class T {
        private int i;

        @Cached(group = "tagG")
        private int getI() {
            return i++;
        }

        private int j;

        @Cached(tags = {"tagA"})
        private int getJ() {
            return j++;
        }
    }

    public void testIsInstrumented() {
        for (Method method : T.class.getDeclaredMethods()) {
            assertNull(method.getAnnotation(Cached.class));
        }
    }

    public void testSimple() {
        T t = new T();
        assert t.getI() == 0;
        assert t.getI() == 0;

        CacheFactory.getCleaner().clearCacheByClass(T.class);

        assert t.getI() == 1;
        assert t.getI() == 1;
    }

    public void testClearByInstance() throws InterruptedException {
        T a = new T();
        T b = new T();

        assert a.getI() == 0;
        assert a.getI() == 0;

        assert b.getI() == 0;
        assert b.getI() == 0;

        CacheFactory.getCleaner().clearCacheByInstance(a);

        assert a.getI() == 1;
        assert a.getI() == 1;

        assert b.getI() == 0;
        assert b.getI() == 0;
    }

    public void testClearByTag() {
        T t = new T();

        assert t.getI() == 0;
        assert t.getI() == 0;

        assert t.getJ() == 0;
        assert t.getJ() == 0;

        CacheFactory.getCleaner().clearCacheByTag("tagA");

        assert t.getI() == 0;
        assert t.getI() == 0;

        assert t.getJ() == 1;
        assert t.getJ() == 1;
    }

    public void testClearByGroup() {
        T t = new T();

        assert t.getI() == 0;
        assert t.getI() == 0;

        assert t.getJ() == 0;
        assert t.getJ() == 0;

        CacheFactory.getCleaner().clearCacheByGroup("tagG");

        assert t.getI() == 1;
        assert t.getI() == 1;

        assert t.getJ() == 0;
        assert t.getJ() == 0;
    }
}
