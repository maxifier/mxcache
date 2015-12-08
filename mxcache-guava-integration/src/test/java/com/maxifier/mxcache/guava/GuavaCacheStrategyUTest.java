/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.guava;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.MxCache;
import com.maxifier.mxcache.hashing.CharArrayHashingStrategy;
import com.maxifier.mxcache.hashing.HashingStrategy;
import com.maxifier.mxcache.hashing.IdentityHashing;
import com.maxifier.mxcache.impl.resource.MxResourceFactory;
import com.maxifier.mxcache.resource.MxResource;
import javax.annotation.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigInteger;
import java.util.Arrays;
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

    public static class EverythingEqualHS implements gnu.trove.strategy.HashingStrategy {
        @Override
        public int computeHashCode(Object object) {
            return 0;
        }
        @Override
        public boolean equals(Object o1, Object o2) {
            return true;
        }
    }

    @Cached(tags = "GUAVA_TAG") @UseGuava
    public String getByArray(int[] array) {
        return Arrays.toString(array) + x++;
    }
    @Cached(tags = "GUAVA_TAG") @UseGuava
    public String getByArray(long[] array) {
        return Arrays.toString(array) + x++;
    }
    @Cached(tags = "GUAVA_TAG") @UseGuava
    public String getByArray(short[] array) {
        return Arrays.toString(array) + x++;
    }
    @Cached(tags = "GUAVA_TAG") @UseGuava
    public String getByArray(double[] array) {
        return Arrays.toString(array) + x++;
    }
    @Cached(tags = "GUAVA_TAG") @UseGuava
    public String getByArray(float[] array) {
        return Arrays.toString(array) + x++;
    }
    @Cached(tags = "GUAVA_TAG") @UseGuava
    public String getByArray(byte[] array) {
        return Arrays.toString(array) + x++;
    }
    @Cached(tags = "GUAVA_TAG") @UseGuava
    public String getByArray(boolean[] array) {
        return Arrays.toString(array) + x++;
    }
    @Cached(tags = "GUAVA_TAG") @UseGuava
    public String getByArray(char[] array) {
        return Arrays.toString(array) + x++;
    }
    @Cached(tags = "GUAVA_TAG") @UseGuava
    public String getByArray(Object[] array) {
        return Arrays.toString(array) + x++;
    }
    @Cached(tags = "GUAVA_TAG") @UseGuava
    public String getByArray(long a, int[] array) {
        return a + Arrays.toString(array) + x++;
    }
    @Cached(tags = "GUAVA_TAG") @UseGuava
    public String getByArray(int a, long[] array) {
        return a + Arrays.toString(array) + x++;
    }
    @Cached(tags = "GUAVA_TAG") @UseGuava
    public String getByArray(String a, short[] array) {
        return a + Arrays.toString(array) + x++;
    }
    @Cached(tags = "GUAVA_TAG") @UseGuava
    public String getByArray(Object[] array1, double[] array2) {
        return Arrays.toString(array1) + x++ + Arrays.toString(array2);
    }
    @Cached(tags = "GUAVA_TAG") @UseGuava
    public String getByArray(float[] array, int a) {
        return a + Arrays.toString(array) + x++;
    }
    @Cached(tags = "GUAVA_TAG") @UseGuava
    public String getByArrayIdentityStr(byte[] array, @IdentityHashing String custom) {
        return custom + Arrays.toString(array) + x++;
    }
    @Cached(tags = "GUAVA_TAG") @UseGuava
    public String getByArrayIdentity(@IdentityHashing boolean[] array) {
        return Arrays.toString(array) + x++;
    }
    @Cached(tags = "GUAVA_TAG") @UseGuava
    public String getByArraySameStrategy(@HashingStrategy(CharArrayHashingStrategy.class) char[] array) {
        return Arrays.toString(array) + x++;
    }
    @Cached(tags = "GUAVA_TAG") @UseGuava
    public String getByArrayIdentity2(Object[] array1, @IdentityHashing Object[] array2) {
        return Arrays.toString(array1) + x++ + Arrays.toString(array2);
    }
    @Cached @UseGuava
    public String getByArrayEqual(@HashingStrategy(EverythingEqualHS.class) Object[] array1) {
        return Arrays.toString(array1) + x++;
    }
    @Cached(tags = "GUAVA_TAG") @UseGuava
    public String getSingleElemIdentity(@IdentityHashing String value) {
        return value + x++;
    }

    @SuppressWarnings("RedundantStringConstructorCall")
    @Test
    public void testArrays() throws Exception {
        String x0 = assertEqualsAndReturn(this.getByArray(new int[]{1}), this.getByArray(new int[]{1}));
        String x1 = assertEqualsAndReturn(this.getByArray(new long[]{1}), this.getByArray(new long[]{1}));
        String x2 = assertEqualsAndReturn(this.getByArray(new short[]{1}), this.getByArray(new short[]{1}));
        String x3 = assertEqualsAndReturn(this.getByArray(new double[]{1}), this.getByArray(new double[]{1}));
        String x4 = assertEqualsAndReturn(this.getByArray(new float[]{1}), this.getByArray(new float[]{1}));
        String x5 = assertEqualsAndReturn(this.getByArray(new byte[]{1}), this.getByArray(new byte[]{1}));
        String x6 = assertEqualsAndReturn(this.getByArray(new boolean[]{true}), this.getByArray(new boolean[]{true}));
        String x7 = assertEqualsAndReturn(this.getByArray(new char[]{'f'}), this.getByArray(new char[]{'f'}));
        String x8 = assertEqualsAndReturn(this.getByArray(new Object[]{new String("1")}), this.getByArray(new Object[]{new String("1")}));
        String x9 = assertEqualsAndReturn(this.getByArray(1, new int[]{2}), this.getByArray(1, new int[]{2}));
        String xA = assertEqualsAndReturn(this.getByArray(2, new long[]{2}), this.getByArray(2, new long[]{2}));
        String xB = assertEqualsAndReturn(this.getByArray(new String("ы"), new short[]{1}), this.getByArray(new String("ы"), new short[]{1}));
        String xC = assertEqualsAndReturn(this.getByArray(new Object[]{new String("ы")}, new double[]{2}), this.getByArray(new Object[]{new String("ы")}, new double[]{2}));
        String xD = assertEqualsAndReturn(this.getByArray(new float[]{2}, 3), this.getByArray(new float[]{2}, 3));
        String xE = assertNotEqualsAndReturn(this.getByArrayIdentityStr(new byte[]{1}, new String("ы")), this.getByArrayIdentityStr(new byte[]{1}, new String("ы")));
        String xF = assertNotEqualsAndReturn(this.getByArrayIdentity(new boolean[]{true}), this.getByArrayIdentity(new boolean[]{true}));
        String xG = assertEqualsAndReturn(this.getByArraySameStrategy(new char[]{'f'}), this.getByArraySameStrategy(new char[]{'f'}));
        String xH = assertNotEqualsAndReturn(this.getByArrayIdentity2(new Object[]{new String("1")}, new Object[]{new String("1")}), this.getByArrayIdentity2(new Object[]{new String("1")}, new Object[]{new String("1")}));
        String xI = assertEqualsAndReturn(this.getByArrayEqual(new Character[]{'f'}), this.getByArrayEqual(new Character[]{'f'}));
        String xJ = assertNotEqualsAndReturn(this.getSingleElemIdentity(new String("123")), this.getSingleElemIdentity(new String("123")));

        CacheFactory.getCleaner().clearCacheByTag("GUAVA_TAG");

        assertNotEquals(x0, this.getByArray(new int[]{1}));
        assertNotEquals(x1, this.getByArray(new long[]{1}));
        assertNotEquals(x2, this.getByArray(new short[]{1}));
        assertNotEquals(x3, this.getByArray(new double[]{1}));
        assertNotEquals(x4, this.getByArray(new float[]{1}));
        assertNotEquals(x5, this.getByArray(new byte[]{1}));
        assertNotEquals(x6, this.getByArray(new boolean[]{true}));
        assertNotEquals(x7, this.getByArray(new char[]{'f'}));
        assertNotEquals(x8, this.getByArray(new Object[]{new String("1")}));
        assertNotEquals(x9, this.getByArray(1, new int[]{2}));
        assertNotEquals(xA, this.getByArray(2, new long[]{2}));
        assertNotEquals(xB, this.getByArray(new String("ы"), new short[]{1}));
        assertNotEquals(xC, this.getByArray(new Object[]{new String("ы")}, new double[]{2}));
        assertNotEquals(xD, this.getByArray(new float[]{2}, 3));
        assertNotEquals(xG, this.getByArraySameStrategy(new char[]{'f'}));
    }

    private static String assertEqualsAndReturn(String a, String b) {
        assertEquals(a, b);
        return a;
    }
    private static String assertNotEqualsAndReturn(String a, String b) {
        assertNotEquals(a, b);
        return a;
    }
}
