/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.clean;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.NonInstrumentedCacheException;
import com.maxifier.mxcache.caches.*;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test(singleThreaded = true)
public class CacheCleanerUTest {

    public static final String TEST_GROUP = "gTest";

    interface X {
    }

    static class O {
        int state;
        static int staticState;
    }

    static class A extends O implements X {
        static final IntCache a;

        final IntCache x;
        final IntCache y;

        static {
            CacheFactory.registerClass(A.class, new Cleanable<A>() {
                        @Override
                        public Cache getStaticCache(int id) {
                            if (id == 2) {
                                return a;
                            }
                            throw new IllegalArgumentException();
                        }

                        @Override
                        public void appendInstanceCachesTo(List<CleaningNode> locks, A a) {
                            locks.add(a.x);
                            locks.add(a.y);
                        }

                        @Override
                        public Cache getInstanceCache(A a, int id) {
                            switch (id) {
                                case 0:
                                    return a.x;
                                case 1:
                                    return a.y;
                                default:
                                    throw new IllegalArgumentException();
                            }
                        }
                    },
                    Collections.singletonMap(TEST_GROUP, new ClassCacheIds(new int[]{0}, new int[]{2})),
                    Collections.singletonMap("test", new ClassCacheIds(new int[]{1}, new int[]{2})));
            CacheFactory.registerCache(A.class, 0, null, int.class, TEST_GROUP, null, new IntCalculatable() {
                @Override
                public int calculate(Object owner) {
                    return ((A) owner).x();
                }
            }, "x", "()I");
            CacheFactory.registerCache(A.class, 1, null, int.class, null, new String[]{"test"}, new IntCalculatable() {
                @Override
                public int calculate(Object owner) {
                    return ((A) owner).y();
                }
            }, "y", "()I");
            CacheFactory.registerCache(A.class, 2, null, int.class, TEST_GROUP, new String[]{"test"}, new IntCalculatable() {
                @Override
                public int calculate(Object owner) {
                    return A.a();
                }
            }, "a", "()I");
            a = (IntCache) CacheFactory.createCache(A.class, 2, null, CacheFactory.getDefaultContext());
        }

        A() {
            CacheFactory.registerInstance(this, A.class);
            x = (IntCache) CacheFactory.createCache(A.class, 0, this, CacheFactory.getDefaultContext());
            y = (IntCache) CacheFactory.createCache(A.class, 1, this, CacheFactory.getDefaultContext());
        }

        static int a() {
            return staticState;
        }

        int x() {
            return state;
        }

        int y() {
            return state;
        }

    }

    static class B extends A {
        static final IntCache b;

        final IntCache z;
        final IntCache w;

        static {
            CacheFactory.registerClass(B.class, new Cleanable<B>() {
                        @Override
                        public Cache getStaticCache(int id) {
                            if (id == 2) {
                                return B.b;
                            }
                            throw new IllegalArgumentException();
                        }

                        @Override
                        public void appendInstanceCachesTo(List<CleaningNode> locks, B b) {
                            locks.add(b.z);
                            locks.add(b.w);
                        }

                        @Override
                        public Cache getInstanceCache(B b, int id) {
                            switch (id) {
                                case 0:
                                    return b.z;
                                case 1:
                                    return b.w;
                                default:
                                    throw new IllegalArgumentException();
                            }
                        }
                    },
                    Collections.singletonMap(TEST_GROUP, new ClassCacheIds(new int[]{0}, new int[]{})),
                    Collections.singletonMap("test", new ClassCacheIds(new int[]{0}, new int[]{2})));
            CacheFactory.registerCache(B.class, 0, null, int.class, "gTest", new String[]{"test"}, new IntCalculatable() {
                @Override
                public int calculate(Object owner) {
                    return ((B) owner).z();
                }
            }, "z", "()I");
            CacheFactory.registerCache(B.class, 1, null, int.class, null, null, new IntCalculatable() {
                @Override
                public int calculate(Object owner) {
                    return ((B) owner).w();
                }
            }, "w", "()I");
            CacheFactory.registerCache(B.class, 2, null, int.class, null, new String[]{"test"}, new IntCalculatable() {
                @Override
                public int calculate(Object owner) {
                    return B.b();
                }
            }, "b", "()I");
            b = (IntCache) CacheFactory.createCache(B.class, 2, null, CacheFactory.getDefaultContext());
        }

        public B() {
            CacheFactory.registerInstance(this, A.class);
            z = (IntCache) CacheFactory.createCache(B.class, 0, this, CacheFactory.getDefaultContext());
            w = (IntCache) CacheFactory.createCache(B.class, 1, this, CacheFactory.getDefaultContext());
        }

        static int b() {
            return staticState;
        }

        int z() {
            return state;
        }

        int w() {
            return state;
        }

    }

    public void testInvalidCleanClass() {
        CleanableRegister register = new CleanableRegister();
        // instance may not yet be loaded, so it's ok to have no caches in interface
        register.clearCacheByClass(Object.class);
        register.clearCacheByClass(Runnable.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidCleanInstance() {
        CleanableRegister register = new CleanableRegister();
        register.clearCacheByInstance(new Object());
    }

    static class NonInstrumented {
        @Cached
        public int x(int x) {
            return x;
        }
    }

    @Test(expectedExceptions = NonInstrumentedCacheException.class)
    public void testCheckNonInstrumented() {
        CleanableRegister.checkNonInstrumentedCaches(NonInstrumented.class);
    }

    @Test(expectedExceptions = NonInstrumentedCacheException.class)
    public void testNonInstrumentedByGroup() {
        CacheFactory.getCleaner().clearInstanceByGroup(new NonInstrumented(), "test");
    }

    @Test(expectedExceptions = NonInstrumentedCacheException.class)
    public void testNonInstrumentedByTag() {
        CacheFactory.getCleaner().clearInstanceByTag(new NonInstrumented(), "test");
    }

    @Test(expectedExceptions = NonInstrumentedCacheException.class)
    public void testNonInstrumentedByInstance() {
        CacheFactory.getCleaner().clearCacheByInstance(new NonInstrumented());
    }

    @Test(expectedExceptions = NonInstrumentedCacheException.class)
    public void testNonInstrumentedByClass() {
        CacheFactory.getCleaner().clearCacheByClass(NonInstrumented.class);
    }

    public void testClearInstanceByTag() {
        B b1 = new B();
        B b2 = new B();

        assertEquals(b1.x.getOrCreate(), 0);
        assertEquals(b1.y.getOrCreate(), 0);
        assertEquals(b1.z.getOrCreate(), 0);
        assertEquals(b1.w.getOrCreate(), 0);

        assertEquals(b2.x.getOrCreate(), 0);
        assertEquals(b2.y.getOrCreate(), 0);
        assertEquals(b2.z.getOrCreate(), 0);
        assertEquals(b2.w.getOrCreate(), 0);

        b1.state++;
        b2.state++;

        CacheFactory.getCleaner().clearInstanceByTag(b1, "test");

        assertEquals(b1.x.getOrCreate(), 0);
        assertEquals(b1.y.getOrCreate(), 1);
        assertEquals(b1.z.getOrCreate(), 1);
        assertEquals(b1.w.getOrCreate(), 0);

        // b2 is not affected
        assertEquals(b2.x.getOrCreate(), 0);
        assertEquals(b2.y.getOrCreate(), 0);
        assertEquals(b2.z.getOrCreate(), 0);
        assertEquals(b2.w.getOrCreate(), 0);
    }

    public void testClearByTag() {
        B b1 = new B();
        B b2 = new B();

        assertEquals(b1.x.getOrCreate(), 0);
        assertEquals(b1.y.getOrCreate(), 0);
        assertEquals(b1.z.getOrCreate(), 0);
        assertEquals(b1.w.getOrCreate(), 0);

        assertEquals(b2.x.getOrCreate(), 0);
        assertEquals(b2.y.getOrCreate(), 0);
        assertEquals(b2.z.getOrCreate(), 0);
        assertEquals(b2.w.getOrCreate(), 0);

        b1.state++;
        b2.state++;

        CacheFactory.getCleaner().clearCacheByTag("test");

        assertEquals(b1.x.getOrCreate(), 0);
        assertEquals(b1.y.getOrCreate(), 1);
        assertEquals(b1.z.getOrCreate(), 1);
        assertEquals(b1.w.getOrCreate(), 0);

        assertEquals(b2.x.getOrCreate(), 0);
        assertEquals(b2.y.getOrCreate(), 1);
        assertEquals(b2.z.getOrCreate(), 1);
        assertEquals(b2.w.getOrCreate(), 0);
    }

    public void testClearInstanceByGroup() {
        B b = new B();

        assertEquals(b.x.getOrCreate(), 0);
        assertEquals(b.y.getOrCreate(), 0);
        assertEquals(b.z.getOrCreate(), 0);
        assertEquals(b.w.getOrCreate(), 0);

        b.state++;

        CacheFactory.getCleaner().clearInstanceByGroup(b, TEST_GROUP);

        assertEquals(b.x.getOrCreate(), 1);
        assertEquals(b.y.getOrCreate(), 0);
        assertEquals(b.z.getOrCreate(), 1);
        assertEquals(b.w.getOrCreate(), 0);
    }

    public void testClearByClass() {
        A a = new A();
        B b = new B();

        assertEquals(a.x.getOrCreate(), 0);
        assertEquals(a.y.getOrCreate(), 0);

        assertEquals(b.x.getOrCreate(), 0);
        assertEquals(b.y.getOrCreate(), 0);
        assertEquals(b.z.getOrCreate(), 0);
        assertEquals(b.w.getOrCreate(), 0);

        // static caches
        assertEquals(A.a.getOrCreate(), 0);
        assertEquals(B.b.getOrCreate(), 0);

        O.staticState++;
        a.state++;
        b.state++;

        CacheFactory.getCleaner().clearCacheByClass(B.class);

        // A is not cleared
        assertEquals(a.x.getOrCreate(), 0);
        assertEquals(a.y.getOrCreate(), 0);

        assertEquals(b.x.getOrCreate(), 1);
        assertEquals(b.y.getOrCreate(), 1);
        assertEquals(b.z.getOrCreate(), 1);
        assertEquals(b.w.getOrCreate(), 1);

        // static caches: A is not touched
        assertEquals(A.a.getOrCreate(), 0);
        assertEquals(B.b.getOrCreate(), 1);

        O.staticState++;
        a.state++;
        b.state++;

        CacheFactory.getCleaner().clearCacheByClass(A.class);

        assertEquals(a.x.getOrCreate(), 2);
        assertEquals(a.y.getOrCreate(), 2);

        // B is subclass of A, so it is cleared also
        assertEquals(b.x.getOrCreate(), 2);
        assertEquals(b.y.getOrCreate(), 2);
        assertEquals(b.z.getOrCreate(), 2);
        assertEquals(b.w.getOrCreate(), 2);

        // static caches: B is not touched now
        assertEquals(A.a.getOrCreate(), 2);
        assertEquals(B.b.getOrCreate(), 1);

        O.staticState++;
        a.state++;
        b.state++;

        CacheFactory.getCleaner().clearCacheByClass(O.class);

        // A is subclass of O, so it is invalidated
        assertEquals(a.x.getOrCreate(), 3);
        assertEquals(a.y.getOrCreate(), 3);

        // B is subclass of O, so it is invalidated
        assertEquals(b.x.getOrCreate(), 3);
        assertEquals(b.y.getOrCreate(), 3);
        assertEquals(b.z.getOrCreate(), 3);
        assertEquals(b.w.getOrCreate(), 3);

        // static caches: neither A nor B are not touched now
        assertEquals(A.a.getOrCreate(), 2);
        assertEquals(B.b.getOrCreate(), 1);
    }

    public void testClearByInterface() {
        A a = new A();
        B b = new B();

        assertEquals(a.x.getOrCreate(), 0);
        assertEquals(a.y.getOrCreate(), 0);

        assertEquals(b.x.getOrCreate(), 0);
        assertEquals(b.y.getOrCreate(), 0);
        assertEquals(b.z.getOrCreate(), 0);
        assertEquals(b.w.getOrCreate(), 0);

        a.state++;
        b.state++;

        CacheFactory.getCleaner().clearCacheByClass(X.class);

        // A implements X, so it is cleared
        assertEquals(a.x.getOrCreate(), 1);
        assertEquals(a.y.getOrCreate(), 1);

        // B implements X, so it is cleared
        assertEquals(b.x.getOrCreate(), 1);
        assertEquals(b.y.getOrCreate(), 1);
        assertEquals(b.z.getOrCreate(), 1);
        assertEquals(b.w.getOrCreate(), 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidRegisterCache() {
        CleanableRegister register = new CleanableRegister();
        register.registerInstance("123", String.class);
    }

}
