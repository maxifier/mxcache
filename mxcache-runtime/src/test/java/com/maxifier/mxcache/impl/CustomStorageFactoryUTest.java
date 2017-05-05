/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.provider.CacheDescriptor;
import org.testng.annotations.Test;

import java.lang.reflect.Constructor;

import static com.maxifier.mxcache.impl.CustomStorageFactory.*;
import static org.testng.Assert.*;

/**
 * CustomStorageFactoryUTest
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-03-24 09:34)
 */
public class CustomStorageFactoryUTest {
    @Test
    public void testGetCustomConstructor_notSuitable() throws Exception {
        assertNull(getCustomConstructor(NoSuitableCtor1.class));
        assertNull(getCustomConstructor(NoSuitableCtor2.class));
    }

    @Test
    public void testGetCustomConstructor_singleSuitable() throws Exception {
        assertEquals(getCustomConstructor(SuitableCtor1.class),
                SuitableCtor1.class.getConstructor(CacheDescriptor.class));

        assertEquals(getCustomConstructor(SuitableCtor2.class),
                SuitableCtor2.class.getConstructor(CacheContext.class));

        assertEquals(getCustomConstructor(SuitableCtor3.class),
                SuitableCtor3.class.getConstructor());
    }

    @Test
    public void testGetCustomConstructor_multipleSuitable_longestChoosen() throws Exception {
        assertEquals(getCustomConstructor(MultiSuitableCtor1.class),
                MultiSuitableCtor1.class.getConstructor(CacheContext.class, CacheDescriptor.class));

        assertEquals(getCustomConstructor(MultiSuitableCtor2.class),
                MultiSuitableCtor2.class.getConstructor(CacheDescriptor.class, CacheContext.class));

        assertEquals(getCustomConstructor(MultiSuitableCtor3.class),
                MultiSuitableCtor3.class.getConstructor(CacheContext.class));
    }

    @Test
    public void testGetCustomConstructor_multipleSuitable_randomChosen() throws Exception {
        Constructor<? extends MultiSuitableCtor4> c = getCustomConstructor(MultiSuitableCtor4.class);
        assertNotNull(c);
        assertTrue(c.equals(MultiSuitableCtor4.class.getConstructor(CacheContext.class)) ||
                   c.equals(MultiSuitableCtor4.class.getConstructor(CacheDescriptor.class)));
    }

    public static class NoSuitableCtor1 {
        public NoSuitableCtor1(CacheDescriptor d, String x) {}
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class NoSuitableCtor2 {
        public NoSuitableCtor2(String x) {}
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class SuitableCtor1 {
        public SuitableCtor1(CacheDescriptor d) {}
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class SuitableCtor2 {
        public SuitableCtor2(CacheContext c) {}

        // this ctor is not suitable
        public SuitableCtor2(String x) {}
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class SuitableCtor3 {
        public SuitableCtor3() {}

        // this ctor is not suitable
        public SuitableCtor3(String x) {}
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class MultiSuitableCtor1 {
        public MultiSuitableCtor1(CacheContext c, CacheDescriptor d) {}
        public MultiSuitableCtor1(CacheContext c) {}
        public MultiSuitableCtor1(CacheDescriptor c) {}
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class MultiSuitableCtor2 {
        public MultiSuitableCtor2(CacheContext c) {}
        public MultiSuitableCtor2(CacheDescriptor c) {}
        public MultiSuitableCtor2(CacheDescriptor d, CacheContext c) {}

        // this ctor is not suitable
        public MultiSuitableCtor2(String x) {}
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class MultiSuitableCtor3 {
        public MultiSuitableCtor3(CacheContext c) {}
        public MultiSuitableCtor3() {}

        // this ctor is not suitable
        public MultiSuitableCtor3(String x) {}
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class MultiSuitableCtor4 {
        public MultiSuitableCtor4(CacheContext c) {}
        public MultiSuitableCtor4(CacheDescriptor c) {}

        // this ctor is not suitable
        public MultiSuitableCtor4(String x) {}
    }
}
