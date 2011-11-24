package com.maxifier.mxcache.test;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.DependencyTracking;
import com.maxifier.mxcache.clean.CacheCleaner;
import com.maxifier.mxcache.resource.TrackDependency;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 31.08.2010
 * Time: 16:14:31
 */
@Test(enabled = false)
public class PerformanceFTest {
    private static final CacheCleaner CLEANER = CacheFactory.getCleaner();

    private static final String[] KEYS;
    private static final int MAX_TUPLE_INT_MISS_OVERHEAD = 8000;
    private static final int MAX_TUPLE_INT_HIT_OVERHEAD = 3500;

    private static final int MAX_INT_INT_MISS_OVERHEAD = 7000;
    private static final int MAX_INT_INT_HIT_OVERHEAD = 3000;

    private static final int MAX_DEPENDENCY_OVERHEAD = 3000;
    private static final int MAX_CLEAR_OVERHEAD = 300000;

    private static final int MAX_OBJECT_OBJECT_MISS_OVERHEAD = 10000;
    private static final int MAX_OBJECT_OBJECT_HIT_OVERHEAD = 2000;

    private static final int MAX_OBJECT_HIT_OVERHEAD = 1000;

    private static final int N = 50000;
    private static final int M = 1000;

    static {
        KEYS = new String[N];
        for (int i = 0; i < N; i++) {
            String s = Integer.toString(i);
            //noinspection ResultOfMethodCallIgnored
            s.hashCode();
            KEYS[i] = s;
        }
    }

    static class T {
        @Cached
        @TrackDependency(DependencyTracking.INSTANCE)
        public static String testChainA(int x) {
            return "123" + x;
        }

        @Cached
        @TrackDependency(DependencyTracking.INSTANCE)
        public String testChainB(int x) {
            return testChainA(x);
        }

        @Cached
        public String getStringToString(String src) {
            return src;
        }

        public String missStringToString(String src) {
            return src;
        }

        @Cached
        public int getIntToInt(int src) {
            return src;
        }

        public int missIntToInt(int src) {
            return src;
        }

        @Cached
        public int getTupleToInt(int a, int b) {
            return a + b;
        }

        public int missInt(int a, int b) {
            return a + b;
        }

        @Cached
        public String getString() {
            return "123";
        }

        public String missString() {
            return "123";
        }
    }

    public void testMisses() {
        T t = new T();

        long t1 = 0;
        for (int i = 0; i < 10; i++) {
            clear(t);
            long start = System.nanoTime();
            batchGet(t, N, 1);
            long end = System.nanoTime();
            t1 += end - start;
        }

        long start = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            batchMiss(t, N, 1);
        }
        long end = System.nanoTime();
        long t2 = end - start;

        long overhead = (t1 - t2) / N;

        checkTime("Miss overhead", overhead, MAX_OBJECT_OBJECT_MISS_OVERHEAD);
    }

    public void testMissesInt() {
        T t = new T();

        long t1 = 0;
        for (int i = 0; i < 10; i++) {
            clear(t);
            long start = System.nanoTime();
            batchGetInt(t, N, 1);
            long end = System.nanoTime();
            t1 += end - start;
        }

        long start = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            batchMissInt(t, N, 1);
        }
        long end = System.nanoTime();
        long t2 = end - start;

        long overhead = (t1 - t2) / N;

        checkTime("Miss int overhead", overhead, MAX_INT_INT_MISS_OVERHEAD);
    }

    public void testMissesTuple() {
        T t = new T();

        long t1 = 0;
        for (int i = 0; i < 10; i++) {
            clear(t);
            long start = System.nanoTime();
            batchGetTuple(t, N, 1);
            long end = System.nanoTime();
            t1 += end - start;
        }

        long start = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            batchMissTuple(t, N, 1);
        }
        long end = System.nanoTime();
        long t2 = end - start;

        long overhead = (t1 - t2) / N;

        checkTime("Miss tuple overhead", overhead, MAX_TUPLE_INT_MISS_OVERHEAD);
    }

    public void testHitsNoArg() {
        T t = new T();

        batchGet(t, N);

        long t1 = 0;
        for (int i = 0; i < 10; i++) {
            long start = System.nanoTime();
            batchGet(t, N);
            long end = System.nanoTime();
            t1 += end - start;
        }
        long overhead = t1 / N;

        checkTime("Hit no arg overhead", overhead, MAX_OBJECT_HIT_OVERHEAD);
    }

    public void testHits() {
        T t = new T();

        clear(t);
        batchGet(t, 5000, 1);

        long start = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            batchGet(t, N / 10, 10);
        }
        long end = System.nanoTime();
        long overhead = (end - start) / N;

        checkTime("Hit overhead", overhead, MAX_OBJECT_OBJECT_HIT_OVERHEAD);
    }

    public void testHitsTuple() {
        T t = new T();

        clear(t);
        batchGetTuple(t, 5000, 1);

        long start = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            batchGetTuple(t, N / 10, 10);
        }
        long end = System.nanoTime();
        long overhead = (end - start) / N;

        checkTime("Hit tuple overhead", overhead, MAX_TUPLE_INT_HIT_OVERHEAD);
    }

    public void testHitsInt() {
        T t = new T();

        clear(t);
        batchGet(t, 5000, 1);

        long start = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            batchGetInt(t, N / 10, 10);
        }
        long end = System.nanoTime();
        long overhead = (end - start) / N;

        checkTime("Hit int overhead", overhead, MAX_INT_INT_HIT_OVERHEAD);
    }

    public void testHitsIntWithProxy() {
        T t = new T();

        clear(t);
        batchGet(t, 5000, 1);

        long start = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            batchGetInt(t, N / 10, 10);
        }
        long end = System.nanoTime();
        long overhead = (end - start) / N;

        checkTime("Hit int overhead", overhead, MAX_INT_INT_HIT_OVERHEAD);
    }

    public void testDependencyTracking() {
        T t = new T();

        long start = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            clear(t);
            batchChain(t, N / 10, 1);
        }
        long end = System.nanoTime();
        long overhead = (end - start) / N;

        checkTime("Chain with dependency", overhead, MAX_DEPENDENCY_OVERHEAD);
    }

    public void testClearByClass() {
        T t = new T();

        clear(t);

        long start = System.nanoTime();
        batchClear(M);
        long end = System.nanoTime();
        long overhead = (end - start) / M;

        checkTime("Clear by class", overhead, MAX_CLEAR_OVERHEAD);
    }

    private static void batchClear(int n) {
        for (int i = 0; i<n; i++) {
            CLEANER.clearCacheByClass(T.class);
        }
    }

    @SuppressWarnings({"UnusedParameters"})
    private void clear(T t) {
//        CLEANER.clearCacheByInstance(t);
        CLEANER.clearCacheByClass(T.class);
    }

    private static void batchChain(T t, int width, int n) {
        while (n --> 0) {
            for (int i = 0; i<width; i++) {
                t.testChainB(i);
            }
        }
    }

    @BeforeClass
    public void makeItHot() {
        T t = new T();
        for (int i = 0; i < 100; i++) {
            batchGet(t, 1000, 10);
            batchMiss(t, 1000, 10);
            batchGetTuple(t, 1000, 10);

            batchGet(t, 10000);
            batchMiss(t, 10000);
            batchChain(t, 5000, 10);
            batchClear(M);
            clear(t);
        }
    }

    private static void batchGet(T t, int width, int n) {
        while (n-- > 0) {
            for (int i = 0; i < width; i++) {
                t.getStringToString(KEYS[i]);
            }
        }
    }

    private static void batchGetInt(T t, int width, int n) {
        while (n-- > 0) {
            for (int i = 0; i < width; i++) {
                t.getIntToInt(i);
            }
        }
    }

    private static void batchGetTuple(T t, int width, int n) {
        while (n-- > 0) {
            for (int i = 0; i < width; i++) {
                t.getTupleToInt(i, i / 2);
            }
        }
    }

    private static void batchGet(T t, int n) {
        while (n-- > 0) {
            t.getString();
        }
    }

    private static void batchMiss(T t, int width, int n) {
        while (n-- > 0) {
            for (int i = 0; i < width; i++) {
                t.missStringToString(KEYS[i]);
            }
        }
    }

    private static void batchMissInt(T t, int width, int n) {
        while (n-- > 0) {
            for (int i = 0; i < width; i++) {
                t.missIntToInt(i);
            }
        }
    }

    private static void batchMissTuple(T t, int width, int n) {
        while (n-- > 0) {
            for (int i = 0; i < width; i++) {
                t.missInt(i, i / 2);
            }
        }
    }

    private static void batchMiss(T t, int n) {
        while (n-- > 0) {
            t.missString();
        }
    }

    private static void checkTime(String label, long time, int maxTime) {
        System.out.println(label + " = " + time + " / " + maxTime);
        System.out.flush();
        Assert.assertTrue(time < maxTime, label + " (" + time + ") should be less than " + maxTime);
    }
}
