/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.test;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.Cached;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.testng.Assert.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@SuppressWarnings({ "AssignmentToStaticFieldFromInstanceMethod", "StaticNonFinalField" })
@Test
public class CachedKeyToValueFTest {
    static int add = 2;

    @Cached
    public static int s(int i) {
        return i + add;
    }

    @Cached
    public int a(int i) {
        return i + add;
    }

    // Use the same name to check how overloading works together with @Cached
    @Cached
    public double a(double i) {
        return i + add;
    }

    @Cached
    public long al(long i) {
        return i + add;
    }

    @Cached
    int add(int a, int b) {
        return a + b + add;
    }

    @Cached
    public Integer b(Integer i) {
        if (i == null) {
            return ~add;
        }
        if (i <= -add) {
            return null;
        }
        return i + add;
    }

    public void testIsInstrumented() {
        for (Method method : getClass().getDeclaredMethods()) {
            assertNull(method.getAnnotation(Cached.class));
        }
    }

    public void testStaticInt() {
        CacheFactory.getCleaner().clearCacheByInstance(this);

        add = 2;

        assertEquals(s(2), 4);
        assertEquals(s(3), 5);

        add = 3;

        assertEquals(s(2), 4);
        assertEquals(s(3), 5);

        CacheFactory.getCleaner().clearCacheByClass(CachedKeyToValueFTest.class);

        assertEquals(s(2), 5);
        assertEquals(s(3), 6);
    }

    public void testInt() {
        CacheFactory.getCleaner().clearCacheByInstance(this);

        add = 2;

        assertEquals(a(2), 4);
        assertEquals(a(3), 5);

        add = 3;

        assertEquals(a(2), 4);
        assertEquals(a(3), 5);

        CacheFactory.getCleaner().clearCacheByInstance(this);

        assertEquals(a(2), 5);
        assertEquals(a(3), 6);
    }

    public void testDouble() {
        CacheFactory.getCleaner().clearCacheByInstance(this);

        add = 2;

        assertEquals(a(2.0), 4.0);
        assertEquals(a(3.0), 5.0);

        add = 3;

        assertEquals(a(2.0), 4.0);
        assertEquals(a(3.0), 5.0);

        CacheFactory.getCleaner().clearCacheByInstance(this);

        assertEquals(a(2), 5);
        assertEquals(a(3), 6);
    }

    public void testPairIntToInt() {
        CacheFactory.getCleaner().clearCacheByInstance(this);

        add = 2;

        assertEquals(add(1, 1), 4);
        assertEquals(add(2, 1), 5);

        add = 3;

        assertEquals(add(1, 1), 4);
        assertEquals(add(2, 1), 5);

        CacheFactory.getCleaner().clearCacheByInstance(this);

        assertEquals(add(1, 1), 5);
        assertEquals(add(2, 1), 6);
    }

    public void testLong() {
        CacheFactory.getCleaner().clearCacheByInstance(this);

        add = 2;

        assertEquals(al(2), 4);
        assertEquals(al(3), 5);

        add = 3;

        assertEquals(al(2), 4);
        assertEquals(al(3), 5);

        CacheFactory.getCleaner().clearCacheByInstance(this);

        assertEquals(al(2), 5);
        assertEquals(al(3), 6);
    }

    public void testWrapper() {
        CacheFactory.getCleaner().clearCacheByInstance(this);

        add = 2;

        assertEquals(b(null), (Integer) ~2);
        assertEquals(b(-2), null);

        assertEquals(b(2), (Integer) 4);
        assertEquals(b(3), (Integer) 5);

        add = 3;

        assertEquals(b(null), (Integer) ~2);
        assertEquals(b(-2), null);

        assertEquals(b(2), (Integer) 4);
        assertEquals(b(3), (Integer) 5);

        CacheFactory.getCleaner().clearCacheByInstance(this);

        assertEquals(b(null), (Integer) ~3);
        assertEquals(b(-2), (Integer) 1);
        assertEquals(b(-3), null);

        assertEquals(b(2), (Integer) 5);
        assertEquals(b(3), (Integer) 6);
    }
}
