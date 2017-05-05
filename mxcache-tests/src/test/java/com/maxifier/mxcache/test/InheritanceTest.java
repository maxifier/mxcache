/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.test;

import com.maxifier.mxcache.Cached;
import org.testng.annotations.Test;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class InheritanceTest {
    static class A {
        int i;

        public int a() {
            return i++;
        }
    }

    static class B extends A {
        @Override
        @Cached
        public int a() {
            return super.a() + 1;
        }
    }

    static class C extends B {
        @Override
        public int a() {
            return super.a() + 1;
        }
    }

    public void testA() {
        A a = new A();
        assert a.a() == 0;
        assert a.a() == 1;
        assert a.a() == 2;
        assert a.a() == 3;
    }

    public void testB() {
        B b = new B();
        assert b.a() == 1;
        assert b.a() == 1;
        assert b.a() == 1;
        assert b.a() == 1;
    }

    public void testC() {
        C c = new C();
        assert c.a() == 2;
        assert c.a() == 2;
        assert c.a() == 2;
        assert c.a() == 2;
    }
}
