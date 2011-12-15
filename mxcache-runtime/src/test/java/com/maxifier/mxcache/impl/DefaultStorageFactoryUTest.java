package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.clean.Cleanable;
import com.maxifier.mxcache.hashing.HashingStrategy;
import com.maxifier.mxcache.hashing.IdentityHashing;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.CacheManager;
import com.maxifier.mxcache.proxy.ProxyFactory;
import com.maxifier.mxcache.proxy.Resolvable;
import com.maxifier.mxcache.transform.WeakKey;
import com.maxifier.mxcache.tuple.Tuple;
import com.maxifier.mxcache.tuple.TupleFactory;
import com.maxifier.mxcache.tuple.TupleGenerator;
import com.maxifier.mxcache.transform.Transform;
import gnu.trove.TObjectIdentityHashingStrategy;
import junit.framework.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.maxifier.mxcache.caches.*;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 21.04.2010
 * Time: 17:06:47
 */
@SuppressWarnings({"unchecked"})
@Test
public class DefaultStorageFactoryUTest {
    private static final int TEST_SIZE_FINGERPRINT = 71;
    public static final int[] TEST_ARRAY = new int[]{1, 2, 3};
    public static final T INSTANCE = new T();

    public static class Key {
        final long v;

        final long x;

        Key(long v, long x) {
            this.v = v;
            this.x = x;
        }

        public long get() {
            return v;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;
            return v == key.v;

        }

        @Override
        public int hashCode() {
            return (int) (v ^ (v >>> 32));
        }

        @Override
        public String toString() {
            return "<" + v + ", " + x + ">"; 
        }
    }

    public static String toStringDiv2(long a) {
        return Long.toString(a >> 1);
    }

    static class T {
        void x() {}

        static void y() {}

        long r(@Transform(method = "get") Key r) {
            return r.x;
        }

        String b(@Transform(owner = DefaultStorageFactoryUTest.class, method = "toStringDiv2") long r) {
            return Long.toString(r);
        }

        String w(@WeakKey Key r) {
            return Long.toString(r.x);
        }

        String w2(@WeakKey Key r, @WeakKey String x, String s) {
            return Long.toString(r.x) + x + s;
        }

        String z(@HashingStrategy(TObjectIdentityHashingStrategy.class) String b) {
            return b;
        }

        int[] w(int[] b) {
            return b;
        }

        String q(@IdentityHashing String b) {
            return b;
        }
    }

    @BeforeClass
    public void before() {
        CacheFactory.registerClass(T.class, mock(Cleanable.class), null, null);
        CacheFactory.registerInstance(INSTANCE, T.class);
    }

    @SuppressWarnings ({ "unchecked", "RedundantStringConstructorCall" })
    public void testHashedArray() {
        ObjectObjectCalculatable<int[], int[]> calculatable = mock(ObjectObjectCalculatable.class);
        CacheDescriptor<T> desc = new CacheDescriptor<T>(T.class, 0, int[].class, int[].class, calculatable, "w", "([I)[I", null, null, null, null);
        assertEquals(desc.getKeyType(), int[].class);
        assertEquals(desc.getValueType(), int[].class);
        assertFalse(desc.isStatic());

        CacheManager<T> d = createDefaultManager(desc);
        ObjectObjectCache<int[], int[]> c = (ObjectObjectCache) d.createCache(INSTANCE);
        assertEquals(d.getInstances().size(), 1);
        // it should be empty
        assertEquals(c.getSize(), 0);
        // and also it should have lock assigned
        assertNotNull(c.getLock());
        int[] a1 = { 3, 4 };
        int[] a2 = { 3, 4 };
        assert a1 != a2;
        assertNull(c.getOrCreate(a1));
        assertNull(c.getOrCreate(a1));
        assertNull(c.getOrCreate(a2));
        assertNull(c.getOrCreate(a2));
        verify(calculatable, times(1)).calculate(INSTANCE, a1);
    }

    public void testUseProxy() {
        ObjectObjectCalculatable<int[], int[]> calculatable = mock(ObjectObjectCalculatable.class);
        CacheDescriptor<T> desc = new CacheDescriptor<T>(T.class, 0, int[].class, int[].class, calculatable, "w", "([I)[I", null, null, null, new ProxyFactory<int[]>() {
            @Override
            public int[] proxy(Class<int[]> expected, Resolvable<int[]> value) {
                Assert.assertNull(value.getValue());
                return TEST_ARRAY;
            }
        });
        CacheManager<T> d = createDefaultManager(desc);
        ObjectObjectCache<int[], int[]> c = (ObjectObjectCache) d.createCache(INSTANCE);
        Assert.assertEquals(c.getOrCreate(null), TEST_ARRAY);
        Assert.assertNotNull(c.getDependencyNode());
    }

    private static <T> CacheManager<T> createDefaultManager(CacheDescriptor<T> desc) {
        return DefaultStrategy.getInstance().getManager(CacheFactory.getDefaultContext(), desc);
    }

    @SuppressWarnings ({ "unchecked", "RedundantStringConstructorCall" })
    public void testCustomStrategy() {
        ObjectObjectCalculatable<String, String> calculatable = mock(ObjectObjectCalculatable.class);
        CacheDescriptor<T> desc = new CacheDescriptor<T>(T.class, 0, String.class, String.class, calculatable, "z", "(Ljava/lang/String;)Ljava/lang/String;", null, null, null, null);
        assert desc.getKeyType() == String.class;
        assert desc.getValueType() == String.class;
        assert !desc.isStatic();

        CacheManager<T> d = createDefaultManager(desc);
        ObjectObjectCache<String, String> c = (ObjectObjectCache) d.createCache(INSTANCE);
        assertEquals(d.getInstances().size(), 1);
        // it should be empty
        assert c.getSize() == 0;
        // and also it should have lock assigned
        assert c.getLock() != null;
        assert c.getOrCreate("123") == null;
        assert c.getOrCreate("123") == null;
        assert c.getOrCreate("123") == null;
        verify(calculatable, times(1)).calculate(INSTANCE, "123");
        assert c.getOrCreate(new String("123")) == null;
        verify(calculatable, times(2)).calculate(INSTANCE, "123");
    }

    @SuppressWarnings ({ "unchecked", "RedundantStringConstructorCall" })
    public void testIdentityHashing() {
        ObjectObjectCalculatable<String, String> calculatable = mock(ObjectObjectCalculatable.class);
        CacheDescriptor<T> desc = new CacheDescriptor<T>(T.class, 0, String.class, String.class, calculatable, "q", "(Ljava/lang/String;)Ljava/lang/String;", null, null, null, null);
        assert desc.getKeyType() == String.class;
        assert desc.getValueType() == String.class;
        assert !desc.isStatic();

        CacheManager<T> d = createDefaultManager(desc);
        ObjectObjectCache<String, String> c = (ObjectObjectCache) d.createCache(INSTANCE);
        assertEquals(d.getInstances().size(), 1);
        // it should be empty
        assert c.getSize() == 0;
        // and also it should have lock assigned
        assert c.getLock() != null;
        assert c.getOrCreate("123") == null;
        assert c.getOrCreate("123") == null;
        assert c.getOrCreate("123") == null;
        verify(calculatable, times(1)).calculate(INSTANCE, "123");
        assert c.getOrCreate(new String("123")) == null;
        verify(calculatable, times(2)).calculate(INSTANCE, "123");
    }

    public static class TestProxyFactory implements ProxyFactory<String> {
        @Override
        public String proxy(Class<String> expected, Resolvable<String> value) {
            return value.getValue() + "proxied";
        }
    }

    @SuppressWarnings({ "unchecked"})
    @Test
    public void testWithProxy() {
        ObjectObjectCalculatable<String, String> calculatable = mock(ObjectObjectCalculatable.class);
        when(calculatable.calculate(INSTANCE, "?")).thenReturn("!");
        CacheDescriptor<T> desc = new CacheDescriptor<T>(T.class, 0, String.class, String.class, calculatable, "q", "(Ljava/lang/String;)Ljava/lang/String;", null, null, null, new TestProxyFactory());
        CacheManager<T> d = createDefaultManager(desc);
        ObjectObjectCache cache = (ObjectObjectCache) d.createCache(INSTANCE);
        assertEquals(d.getInstances().size(), 1);
        assertEquals(cache.getOrCreate("?"), "!proxied");
    }

    private static <K, V> void test(Class<K> key, Class<V> value, Class<? extends Cache> cache) {
        // yes, this test is hacky! the methods isn't correct
        CacheDescriptor<T> desc = new CacheDescriptor<T>(T.class, 0, key, value, null, "x", "()V", null, null, null, null);
        assertEquals(desc.getKeyType(), key);
        assertEquals(desc.getValueType(), value);
        assert !desc.isStatic();
        
        CacheManager<T> d = createDefaultManager(desc);
        Cache c = d.createCache(INSTANCE);
        assertEquals(d.getInstances().size(), 1);
        // it should be empty
        assertEquals(c.getSize(), 0);
        // and also it should have lock assigned
        assertNotNull(c.getLock());
        assertTrue(cache.isInstance(c), "Cache for " + key + " -> " + value + " should be instance of " + cache + " but is " + c.getClass());
    }

    public void testLazyCaches() {
        test(null, byte.class, ByteCache.class);
        test(null, short.class, ShortCache.class);
        test(null, int.class, IntCache.class);
        test(null, long.class, LongCache.class);
        test(null, float.class, FloatCache.class);
        test(null, double.class, DoubleCache.class);
        test(null, Object.class, ObjectCache.class);
        test(null, String.class, ObjectCache.class);
        test(null, Runnable.class, ObjectCache.class);
    }

    public void testMapCaches() {
        test(int.class, int.class, IntIntCache.class);

        //некоторые кэши не поддерживаются
        test(int.class, boolean.class, IntBooleanCache.class);
        test(int.class, byte.class, IntByteCache.class);
        test(int.class, char.class, IntCharacterCache.class);
        test(int.class, short.class, IntShortCache.class);
        test(int.class, long.class, IntLongCache.class);
        test(int.class, float.class, IntFloatCache.class);
        test(int.class, double.class, IntDoubleCache.class);

        test(int.class, Object.class, IntObjectCache.class);
        test(int.class, String.class, IntObjectCache.class);
        test(int.class, Runnable.class, IntObjectCache.class);

        test(byte.class, int.class, ByteIntCache.class);
        test(short.class, int.class, ShortIntCache.class);
        test(long.class, int.class, LongIntCache.class);
        test(float.class, int.class, FloatIntCache.class);
        test(double.class, int.class, DoubleIntCache.class);

        test(Object.class, int.class, ObjectIntCache.class);
        test(String.class, int.class, ObjectIntCache.class);
        test(Runnable.class, int.class, ObjectIntCache.class);

        test(Object.class, String.class, ObjectObjectCache.class);
        test(String.class, Runnable.class, ObjectObjectCache.class);
        test(Runnable.class, Object.class, ObjectObjectCache.class);
    }

    public void testStaticCache() {
        CacheDescriptor<T> descriptor = new CacheDescriptor<T>(T.class, 0, int.class, int.class, null, "y", "()V", null, null, null, null);
        assertTrue(descriptor.isStatic());
        CacheManager<T> d = createDefaultManager(descriptor);
        @SuppressWarnings({"UnusedDeclaration"})
        Cache cache = d.createCache(null);
        assertEquals(d.getInstances().size(), 1);
    }

    public void testTransformToPrimitive() throws InterruptedException {
        CacheDescriptor<T> descriptor = new CacheDescriptor<T>(T.class, 0, Key.class, long.class, new ObjectLongCalculatable<Key>() {
            @Override
            public long calculate(Object owner, Key o) {
                return o.x;
            }
        }, "r", "(Lcom.maxifier.mxcache.impl.DefaultStorageFactoryUTest$Key;)J", null, null, null, null);
        CacheManager<T> d = createDefaultManager(descriptor);
        // noinspection unchecked
        ObjectLongCache<Key> c = (ObjectLongCache<Key>) d.createCache(INSTANCE);

        assertEquals(c.getSize(), 0);

        Key k = new Key(1, 2);
        ReferenceQueue<Key> q = new ReferenceQueue<Key>();
        WeakReference<Key> r = new WeakReference<Key>(k, q);
        assertEquals(c.getOrCreate(k), 2);
        //noinspection UnusedAssignment
        k = null;
        System.gc();
        System.gc();
        assertSame(q.remove(), r);

        // first key is the same!
        assertEquals(c.getOrCreate(new Key(1, 4)), 2);

        assertEquals(c.getSize(), 1);
    }

    public void testTransformToObject() throws InterruptedException {
        CacheDescriptor<T> desc = new CacheDescriptor<T>(T.class, 0, long.class, String.class, new LongObjectCalculatable<String>() {
            @Override
            public String calculate(Object owner, long o) {
                return Long.toString(o);
            }
        }, "b", "(J)Ljava/lang/String;", null, null, null, null);
        CacheManager<T> d = createDefaultManager(desc);
        // noinspection unchecked
        LongObjectCache<String> c = (LongObjectCache<String>) d.createCache(INSTANCE);

        assertEquals(c.getSize(), 0);

        assertEquals(c.getOrCreate(1), "1");
        assertEquals(c.getOrCreate(0), "1");
        assertEquals(c.getSize(), 1);

        assertEquals(c.getOrCreate(70), "70");
        assertEquals(c.getOrCreate(TEST_SIZE_FINGERPRINT), "70");
        assertEquals(c.getSize(), 2);
    }

    public void testWeakCaches() throws InterruptedException {
        CacheDescriptor<T> descriptor = new CacheDescriptor<T>(T.class, 0, Key.class, String.class, new ObjectObjectCalculatable<Key, String>() {
            @Override
            public String calculate(Object owner, Key o) {
                return Long.toString(o.x);
            }
        }, "w", "(Lcom.maxifier.mxcache.impl.DefaultStorageFactoryUTest$Key;)Ljava/lang/String;", null, null, null, null);
        CacheManager<T> d = createDefaultManager(descriptor);
        // noinspection unchecked
        ObjectObjectCache<Key, String> c = (ObjectObjectCache<Key, String>) d.createCache(INSTANCE);

        assertEquals(c.getSize(), 0);

        Key k = new Key(7, 7);
        ReferenceQueue<Key> q = new ReferenceQueue<Key>();
        WeakReference<Key> r = new WeakReference<Key>(k, q);

        assertEquals(c.getOrCreate(k), "7");
        assertEquals(c.getOrCreate(new Key(7, 11)), "7");
        assertEquals(c.getSize(), 1);
        //noinspection UnusedAssignment
        k = null;

        System.gc();
        Thread.sleep(100);
        System.gc();
        assertSame(q.remove(), r);
        System.gc();

        assertEquals(c.getOrCreate(new Key(7, 11)), "11");
        assertEquals(c.getSize(), 1);
    }

    public void testTupleWeakCaches() throws InterruptedException {
        TupleFactory factory = TupleGenerator.getTupleFactory(Key.class, String.class, String.class);

        CacheDescriptor<T> descriptor = new CacheDescriptor<T>(T.class, 0, factory.getTupleClass(), String.class, new ObjectObjectCalculatable<Tuple, String>() {
            @Override
            public String calculate(Object owner, Tuple o) {
                return Long.toString(o.<Key>get(0).x) + o.<String>get(1) + o.<String>get(2);
            }
        }, "w2", "(Lcom.maxifier.mxcache.impl.DefaultStorageFactoryUTest$Key;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", null, null, null, null);
        CacheManager<T> d = createDefaultManager(descriptor);
        // noinspection unchecked
        ObjectObjectCache<Tuple, String> c = (ObjectObjectCache<Tuple, String>) d.createCache(INSTANCE);

        assertEquals(c.getSize(), 0);

        Key k = new Key(7, 7);
        ReferenceQueue<Key> q = new ReferenceQueue<Key>();
        WeakReference<Key> r = new WeakReference<Key>(k, q);

        assertEquals(c.getOrCreate(factory.create(k, "123", "321")), "7123321");
        assertEquals(c.getOrCreate(factory.create(new Key(7, 11), "123", "321")), "7123321");
        assertEquals(c.getSize(), 1);
        //noinspection UnusedAssignment
        k = null;

        System.gc();
        Thread.sleep(100);
        System.gc();
        assertSame(q.remove(), r);
        System.gc();

        assertEquals(c.getOrCreate(factory.create(new Key(7, 11), "123", "321")), "11123321");
        assertEquals(c.getSize(), 1);
    }
}
