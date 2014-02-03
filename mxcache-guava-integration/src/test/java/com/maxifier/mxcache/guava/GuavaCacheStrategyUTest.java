/*
 * Copyright (c) 2008-2013 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.guava;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.MxCache;
import com.maxifier.mxcache.impl.resource.MxResourceFactory;
import com.maxifier.mxcache.resource.MxResource;
import javax.annotation.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigInteger;
import java.util.concurrent.CountDownLatch;

import static org.testng.Assert.*;

/**
 * GuavaCacheStrategyUTest
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2012-10-09 10:03)
 */
@Test(singleThreaded = true)
public class GuavaCacheStrategyUTest {
    private int x = 0;

    private final MxResource r = MxResourceFactory.getResource("test");

    @Cached
    @UseGuava(maxSize = 1)
    public int sumII(int a) {
        return a + x;
    }

    @Cached
    @UseGuava(maxSize = 1)
    public long sumZJ(boolean a) {
        return (a ? 1 : 0) + x;
    }

    @Cached
    @UseGuava(maxSize = 1)
    public double sumBD(byte a) {
        return a + x;
    }

    @Cached
    @UseGuava(expireAfterWrite = 100)
    public int testNoArg() {
        return x;
    }

    public void testPrimitive() {
        x = 0;
        assertEquals(sumII(5), 5);
        assertEquals(sumZJ(true), 1L);
        assertEquals(sumBD((byte)3), 3.0);

        x = 2;
        assertEquals(sumII(5), 5);
        assertEquals(sumZJ(true), 1L);
        assertEquals(sumBD((byte)3), 3.0);

        assertEquals(sumII(6), 8);
        assertEquals(sumZJ(false), 2L);
        assertEquals(sumBD((byte)4), 6.0);

        assertEquals(sumII(5), 7);
        assertEquals(sumZJ(true), 3L);
        assertEquals(sumBD((byte)3), 5.0);
    }

    @Cached
    @UseGuava(maxSize = 5)
    @Nullable
    private String testMethod(@Nullable String v) {
        r.readStart();
        try {
            if (v == null) {
                return null;
            }
            if (v.startsWith("^")) {
                throw new IllegalArgumentException(v.substring(1));
            }
            if (v.startsWith("#")) {
                throw new OutOfMemoryError(v.substring(1));
            }
            return v + "_" + x;
        } finally {
            r.readEnd();
        }
    }

    @Cached
    @UseGuava(expireAfterWrite = 100)
    private String testMethod2(String s) {
        return s + x;
    }

    public void testExpiration() throws InterruptedException {
        int retriesLeft = 10;
        while (retriesLeft --> 0) {
            x = 0;
            CacheFactory.getCleaner().clearCacheByInstance(this);
            long t = System.currentTimeMillis();
            assertEquals(testMethod2("test"), "test0");
            x++;
            String v = testMethod2("test");
            long t2 = System.currentTimeMillis();
            if (t2 - t < 100) {
                assertEquals(v, "test0");
                break;
            }
        }
        assertTrue(retriesLeft > 0, "Too many retries made");

        x = 0;
        CacheFactory.getCleaner().clearCacheByInstance(this);
        assertEquals(testMethod2("test"), "test0");
        x++;
        Thread.sleep(150);
        assertEquals(testMethod2("test"), "test1");
    }

    public void testExpirationNoArg() throws InterruptedException {
        int retriesLeft = 10;
        while (retriesLeft --> 0) {
            x = 0;
            CacheFactory.getCleaner().clearCacheByInstance(this);
            long t = System.currentTimeMillis();
            assertEquals(testNoArg(), 0);
            x++;
            int v = testNoArg();
            long t2 = System.currentTimeMillis();
            if (t2 - t < 100) {
                assertEquals(v, 0);
                break;
            }
        }
        assertTrue(retriesLeft > 0, "Too many retries made");

        x = 0;
        CacheFactory.getCleaner().clearCacheByInstance(this);
        assertEquals(testNoArg(), 0);
        x++;
        Thread.sleep(150);
        assertEquals(testNoArg(), 1);
    }

    public void testExceptionTransparency() {
        try {
            testMethod("^Test");
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Test");
        }

        try {
            testMethod("#Test");
            fail("Exception expected");
        } catch (OutOfMemoryError e) {
            assertEquals(e.getMessage(), "Test");
        }
    }

    public void testNull() {
        CacheFactory.getCleaner().clearCacheByInstance(this);

        x = 0;

        assertEquals(testMethod("a"), "a_0");
        assertNull(testMethod(null));
    }

    public void testDependency() {
        x = 0;

        CacheFactory.getCleaner().clearCacheByInstance(this);

        assertEquals(testMethod("a"), "a_0");

        x = 1;

        assertEquals(testMethod("a"), "a_0");

        r.clearDependentCaches();

        assertEquals(testMethod("a"), "a_1");
    }

    public void testClearCache() {
        x = 0;

        CacheFactory.getCleaner().clearCacheByInstance(this);

        assertEquals(testMethod("a"), "a_0");

        x = 1;

        assertEquals(testMethod("a"), "a_0");

        CacheFactory.getCleaner().clearCacheByInstance(this);

        assertEquals(testMethod("a"), "a_1");
    }

    // CPO-13921: there was a deadlock in case of resource modification conflict
    // I set timeout to 1 min because it's enough to detect a deadlock, and during normal execution (with no deadlock)
    // this test will pass in few ms.
    @Test(timeOut = 60000)
    public void testResourceWriteConflict() throws InterruptedException {
        x = 0;
        CacheFactory.getCleaner().clearCacheByInstance(this);

        // needed to register dependency
        assertEquals(testMethod("x"), "x_0");

        final CountDownLatch latch = new CountDownLatch(1);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                r.writeStart();
                try {
                    latch.countDown();
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } finally {
                    r.writeEnd();
                }
            }
        };

        Thread t = new Thread(runnable);
        t.start();

        // wait for resource to be locked
        latch.await();

        // now it should wait for resource to be released
        assertEquals(testMethod("a"), "a_0");

        t.join();
    }

    public void testEvictionWithMaxSize() {
        CacheFactory.getCleaner().clearCacheByInstance(this);

        x = 0;

        assertEquals(testMethod("a"), "a_0");
        assertEquals(testMethod("b"), "b_0");
        assertEquals(testMethod("c"), "c_0");
        assertEquals(testMethod("d"), "d_0");
        assertEquals(testMethod("e"), "e_0");

        x++;

        assertEquals(testMethod("g"), "g_1");
        assertEquals(testMethod("g"), "g_1");

        x++;

        assertEquals(testMethod("b"), "b_0");
        assertEquals(testMethod("c"), "c_0");
        assertEquals(testMethod("d"), "d_0");
        assertEquals(testMethod("e"), "e_0");

        // a should be evicted as it is used less frequently
        assertEquals(testMethod("a"), "a_2");
        assertEquals(testMethod("a"), "a_2");

        // but g is now evicted
        assertEquals(testMethod("g"), "g_2");
    }

    @Cached
    public BigInteger testMethodWhichTestsNulls(String s) {
        if (s == null) return BigInteger.ZERO;
        if (s.equals("return null plz")) return null;
        return BigInteger.ONE;
    }

    public void testNullArgumentsAndNullReturns() {
        CacheFactory.getCleaner().clearCacheByInstance(this);

        assertEquals(testMethodWhichTestsNulls(null), BigInteger.ZERO);
        assertEquals(testMethodWhichTestsNulls("return null plz"), null);
        assertEquals(testMethodWhichTestsNulls(""), BigInteger.ONE);

        assertEquals(testMethodWhichTestsNulls(null), BigInteger.ZERO);
        assertEquals(testMethodWhichTestsNulls("return null plz"), null);
        assertEquals(testMethodWhichTestsNulls(""), BigInteger.ONE);
    }

    public void testWithoutCache() {
        MxCache.withoutCache(new Runnable() {
            @Override
            public void run() {
                x = 0;
                Assert.assertEquals(sumII(5), 5);
                x = 1;
                Assert.assertEquals(sumII(5), 6);

                assertNull(testMethod(null));
                assertEquals(testMethodWhichTestsNulls(null), BigInteger.ZERO);
                assertEquals(testMethodWhichTestsNulls("return null plz"), null);
                assertEquals(testMethodWhichTestsNulls(""), BigInteger.ONE);
            }
        });
    }
}
