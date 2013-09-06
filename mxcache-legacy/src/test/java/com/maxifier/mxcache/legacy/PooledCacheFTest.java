package com.maxifier.mxcache.legacy;

import com.magenta.dataserializator.MxDataSerializator;
import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.impl.MutableStatisticsImpl;
import com.maxifier.mxcache.legacy.converters.ConverterException;
import com.maxifier.mxcache.legacy.converters.MxAbstractResourceConverter;
import com.maxifier.mxcache.legacy.converters.MxConverter;
import com.maxifier.mxcache.storage.Storage;
import com.maxifier.mxcache.impl.wrapping.Wrapping;
import com.maxifier.mxcache.legacy.converters.MxConvertType;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.Signature;
import com.maxifier.mxcache.proxy.ConstResolvable;
import com.maxifier.mxcache.proxy.MxProxy;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import java.io.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 02.11.2010
 * Time: 9:47:52
 */
@Test
public class PooledCacheFTest {
    private static final MxStage STAGE = new MxStage() {};
    private static final TestResourceManager TEST_RESOURCE_MANAGER = new TestResourceManager();

    interface IValue {
        String get();
    }

    private static class Value implements IValue {
        private final String value;

        private Value(String value) {
            this.value = value;
        }

        @Override
        public String get() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Value other = (Value) o;

            return value.equals(other.get());

        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public void test() {
        MxDataSerializator serializator = mock(MxDataSerializator.class);
        final MxCachePoolManager<Value> manager = new MxCachePoolManager<Value>("test_cache", 0.0, 0.0, 0, MxConvertType.ONLY_DEFAULT,
                                        mock(MBeanRegistrator.class),
                                        TEST_RESOURCE_MANAGER, new MxCacheFlusher(),
                                        new MockConvertFromByteArray10<Value>(serializator),
                                        new MockConvertToByteArray10<Value>(serializator),
                                        new MockConvertFromResource10<Value>(serializator),
                                        new MockConvertToResource10<Value>(TEST_RESOURCE_MANAGER, serializator));
        MxPooledCacheStrategy<Value, MxConvertType> strategy = new TestStrategy<Value>(manager);
        // noinspection unchecked
        ObjectObjectCalculatable<String, Value> calculatable = mock(ObjectObjectCalculatable.class);
        Signature sign = Signature.of(String.class, Value.class);
        // noinspection unchecked
        ObjectObjectCache<String, Value> cache = (ObjectObjectCache) Wrapping.getFactory(sign, sign, true)
                .wrap(null, calculatable,
                        new PooledCache<String, Value, MxConvertType>(null, manager, MxConvertType.DEFAULT, strategy), new MutableStatisticsImpl());

        when(calculatable.calculate(null, "test")).thenReturn(new Value("test"));
        assertEquals(cache.getOrCreate("test"), new Value("test"));
        assertEquals(cache.getOrCreate("test"), new Value("test"));
        verify(calculatable).calculate(null, "test");
        verifyNoMoreInteractions(calculatable);
        reset(calculatable);

        lockAndClear(cache);

        when(calculatable.calculate(null, "test")).thenReturn(new Value("test2"));
        assertEquals(cache.getOrCreate("test"), new Value("test2"));
        verify(calculatable).calculate(null, "test");
        verifyNoMoreInteractions(calculatable);
        reset(calculatable);

        lockAndClear(cache);
        assertEquals(manager.getConfiguration().getPoolSize(), 0.0);
    }

    public void testClearToYoung() {
        MxDataSerializator serializator = mock(MxDataSerializator.class);
        final MxCachePoolManager<Value> manager = new MxCachePoolManager<Value>("test_cache", 0.0, 0.0, 0, MxConvertType.ONLY_DEFAULT,
                                                                                mock(MBeanRegistrator.class),
                                                                                TEST_RESOURCE_MANAGER, new MxCacheFlusher(),
                                                                                new MockConvertFromByteArray10<Value>(serializator),
                                                                                new MockConvertToByteArray10<Value>(serializator),
                                                                                new MockConvertFromResource10<Value>(serializator),
                                                                                new MockConvertToResource10<Value>(TEST_RESOURCE_MANAGER, serializator));
        manager.getConfiguration().setLimit(2.0);
        MxPooledCacheStrategy<Value, MxConvertType> strategy = new TestStrategy<Value>(manager);
        // noinspection unchecked
        ObjectObjectCalculatable<String, Value> calculatable = mock(ObjectObjectCalculatable.class);
        Signature sign = Signature.of(String.class, Value.class);
        // noinspection unchecked
        ObjectObjectCache<String, Value> cache = (ObjectObjectCache) Wrapping.getFactory(sign, sign, true)
                .wrap(null, calculatable,
                        new PooledCache<String, Value, MxConvertType>(null, manager, MxConvertType.DEFAULT, strategy), new MutableStatisticsImpl());

        when(calculatable.calculate(null, "test")).thenReturn(new Value("test"));
        when(calculatable.calculate(null, "maxifier")).thenReturn(new Value("cool"));

        assertEquals(cache.getOrCreate("test"), new Value("test"));
        assertEquals(cache.getOrCreate("test"), new Value("test"));
        assertEquals(cache.getOrCreate("test"), new Value("test"));
        assertEquals(cache.getOrCreate("test"), new Value("test"));
        assertEquals(cache.getOrCreate("maxifier"), new Value("cool"));

        assertEquals(manager.getYoungCount(), 2);
        assertEquals(cache.getSize(), 2);

        verify(calculatable).calculate(null, "test");
        verify(calculatable).calculate(null, "maxifier");
        verifyNoMoreInteractions(calculatable);
        reset(calculatable);

        manager.getConfiguration().clearTo(0.5);

        // вычищается только один элемент
        assertEquals(manager.getYoungCount(), 1);
        assertEquals(cache.getSize(), 2);

        when(calculatable.calculate(null, "test")).thenReturn(new Value("new test"));
        when(calculatable.calculate(null, "maxifier")).thenReturn(new Value("new cool"));

        assertEquals(cache.getOrCreate("test"), new Value("new test"));
        assertEquals(cache.getOrCreate("maxifier"), new Value("cool"));

        // test используется чаще, но young зависит только от последнего добавленного 
        verify(calculatable).calculate(null, "test");
        verifyNoMoreInteractions(calculatable);
    }

    public void testClearToOld() {
        MxDataSerializator serializator = mock(MxDataSerializator.class);
        final MxCachePoolManager<Value> manager = new MxCachePoolManager<Value>("test_cache", 0.0, 0.0, 0, MxConvertType.ONLY_DEFAULT,
                                                                                mock(MBeanRegistrator.class),
                                                                                TEST_RESOURCE_MANAGER, new MxCacheFlusher(),
                                                                                new MockConvertFromByteArray10<Value>(serializator),
                                                                                new MockConvertToByteArray10<Value>(serializator),
                                                                                new MockConvertFromResource10<Value>(serializator),
                                                                                new MockConvertToResource10<Value>(TEST_RESOURCE_MANAGER, serializator));
        manager.getConfiguration().setLimit(2.0);
        MxPooledCacheStrategy<Value, MxConvertType> strategy = new TestStrategy<Value>(manager);
        // noinspection unchecked
        ObjectObjectCalculatable<String, Value> calculatable = mock(ObjectObjectCalculatable.class);
        Signature sign = Signature.of(String.class, Value.class);
        // noinspection unchecked
        ObjectObjectCache<String, Value> cache = (ObjectObjectCache) Wrapping.getFactory(sign, sign, true)
                .wrap(null, calculatable,
                        new PooledCache<String, Value, MxConvertType>(null, manager, MxConvertType.DEFAULT, strategy), new MutableStatisticsImpl());

        when(calculatable.calculate(null, "test")).thenReturn(new Value("test"));
        when(calculatable.calculate(null, "maxifier")).thenReturn(new Value("cool"));

        for (int i = 0; i<5; i++) {
            // нам нужно загнать элементы в пул
            long start = System.nanoTime();
            manager.periodStart(new MxStageStartEvent(STAGE, start));
            assertEquals(cache.getOrCreate("test"), new Value("test"));
            assertEquals(cache.getOrCreate("test"), new Value("test"));
            assertEquals(cache.getOrCreate("test"), new Value("test"));
            assertEquals(cache.getOrCreate("test"), new Value("test"));
            assertEquals(cache.getOrCreate("maxifier"), new Value("cool"));
            manager.periodFinish(new MxStageEndEvent(STAGE, System.nanoTime(), start));

            manager.stateHandler();
        }

        assertEquals(manager.getOldCount(), 2);
        assertEquals(cache.getSize(), 2);

        verify(calculatable).calculate(null, "test");
        verify(calculatable).calculate(null, "maxifier");
        verifyNoMoreInteractions(calculatable);
        reset(calculatable);

        manager.getConfiguration().clearTo(0.5);

        // вычищается только один элемент
        assertEquals(manager.getOldCount(), 1);
        assertEquals(cache.getSize(), 2);

        when(calculatable.calculate(null, "test")).thenReturn(new Value("new test"));
        when(calculatable.calculate(null, "maxifier")).thenReturn(new Value("new cool"));

        assertEquals(cache.getOrCreate("test"), new Value("test"));
        assertEquals(cache.getOrCreate("maxifier"), new Value("new cool"));

        // test используется чаще, поэтому выживет
        verify(calculatable).calculate(null, "maxifier");
        verifyNoMoreInteractions(calculatable);
    }

    public void testMultithread() throws InterruptedException {
        MxDataSerializator serializator = mock(MxDataSerializator.class);
        final MxCachePoolManager<Value> manager = new MxCachePoolManager<Value>("test_cache", 0.0, 0.0, 0, MxConvertType.ONLY_DEFAULT,
                                                                                mock(MBeanRegistrator.class),
                                                                                TEST_RESOURCE_MANAGER, new MxCacheFlusher(),
                                                                                new MockConvertFromByteArray10<Value>(serializator),
                                                                                new MockConvertToByteArray10<Value>(serializator),
                                                                                new MockConvertFromResource10<Value>(serializator),
                                                                                new MockConvertToResource10<Value>(TEST_RESOURCE_MANAGER, serializator));
        MxPooledCacheStrategy<Value, MxConvertType> strategy = new TestStrategy<Value>(manager);

        final CyclicBarrier barrier = new CyclicBarrier(2);

        final AtomicBoolean b = new AtomicBoolean();

        // noinspection unchecked
        ObjectObjectCalculatable<String, Value> calculatable = new ObjectObjectCalculatable<String, Value>() {
            @Override
            public Value calculate(Object owner, String o) {
                try {
                    barrier.await();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                b.set(true);
                return new Value(o);
            }
        };
        Signature sign = Signature.of(String.class, Value.class);
        // noinspection unchecked
        final ObjectObjectCache<String, Value> cache = (ObjectObjectCache) Wrapping.getFactory(sign, sign, true)
                .wrap(null, calculatable,
                        new PooledCache<String, Value, MxConvertType>(null, manager, MxConvertType.DEFAULT, strategy), new MutableStatisticsImpl());

        Thread thread = new Thread() {
            @Override
            public void run() {
                assertEquals(cache.getOrCreate("test2"), new Value("test2"));
            }
        };
        thread.start();

        assertEquals(cache.getOrCreate("test1"), new Value("test1"));
        assertEquals(cache.getOrCreate("test1"), new Value("test1"));
        assertTrue(b.get());
        
        thread.join();
    }

    public void testTwoCaches() {
        MxDataSerializator serializator = mock(MxDataSerializator.class);
        final MxCachePoolManager<Value> manager = new MxCachePoolManager<Value>("test_cache", 0.0, 0.0, 0, MxConvertType.ONLY_DEFAULT,
                                                                                mock(MBeanRegistrator.class),
                                                                                TEST_RESOURCE_MANAGER, new MxCacheFlusher(),
                                                                                new MockConvertFromByteArray10<Value>(serializator),
                                                                                new MockConvertToByteArray10<Value>(serializator),
                                                                                new MockConvertFromResource10<Value>(serializator),
                                                                                new MockConvertToResource10<Value>(TEST_RESOURCE_MANAGER, serializator));
        MxPooledCacheStrategy<Value, MxConvertType> strategy = new TestStrategy<Value>(manager);
        // noinspection unchecked
        ObjectObjectCalculatable<String, Value> calculatable = mock(ObjectObjectCalculatable.class);
        Signature sign = Signature.of(String.class, Value.class);
        // noinspection unchecked
        ObjectObjectCache<String, Value> cache = (ObjectObjectCache) Wrapping.getFactory(sign, sign, true)
                .wrap(null, calculatable,
                        new PooledCache<String, Value, MxConvertType>(null, manager, MxConvertType.DEFAULT, strategy), new MutableStatisticsImpl());

        // noinspection unchecked
        ObjectObjectCache<String, Value> cache2 = (ObjectObjectCache) Wrapping.getFactory(sign, sign, true)
                .wrap(null, calculatable,
                        new PooledCache<String, Value, MxConvertType>(null, manager, MxConvertType.DEFAULT, strategy), new MutableStatisticsImpl());

        when(calculatable.calculate(null, "test")).thenReturn(new Value("test"));
        assertEquals(cache.getOrCreate("test"), new Value("test"));
        assertEquals(cache.getOrCreate("test"), new Value("test"));
        assertEquals(cache2.getOrCreate("test"), new Value("test"));

        assertEquals(cache.getSize(), 1);
        assertEquals(cache2.getSize(), 1);

        assertEquals(manager.getConfiguration().getPoolSize(), 2.0);

        verify(calculatable, times(2)).calculate(null, "test");
        verifyNoMoreInteractions(calculatable);
        reset(calculatable);

        lockAndClear(cache);
        assertEquals(cache.getSize(), 0);
        assertEquals(cache2.getSize(), 1);

        assertEquals(manager.getConfiguration().getPoolSize(), 1.0);

        when(calculatable.calculate(null, "test")).thenReturn(new Value("test2"));
        assertEquals(cache.getOrCreate("test"), new Value("test2"));
        assertEquals(cache2.getOrCreate("test"), new Value("test"));
        verify(calculatable).calculate(null, "test");
        verifyNoMoreInteractions(calculatable);
        reset(calculatable);

        lockAndClear(cache);
        assertEquals(manager.getConfiguration().getPoolSize(), 1.0);
    }

    public void testUnproxy() {
        MxDataSerializator serializator = mock(MxDataSerializator.class);
        final MxCachePoolManager<IValue> manager = new MxCachePoolManager<IValue>("test_cache", 0.0, 0.0, 0, MxConvertType.ONLY_DEFAULT,
                                                                                mock(MBeanRegistrator.class),
                                                                                TEST_RESOURCE_MANAGER, new MxCacheFlusher(),
                                                                                new MockConvertFromByteArray10<IValue>(serializator),
                                                                                new MockConvertToByteArray10<IValue>(serializator),
                                                                                new MockConvertFromResource10<IValue>(serializator),
                                                                                new MockConvertToResource10<IValue>(TEST_RESOURCE_MANAGER, serializator));
        MxPooledCacheStrategy<IValue, MxConvertType> strategy = new TestStrategy<IValue>(manager);
        // noinspection unchecked
        PooledCache<String, IValue, MxConvertType> c = new PooledCache<String, IValue, MxConvertType>(null, manager, MxConvertType.DEFAULT, strategy);

        c.lock("test");
        assertEquals(c.load("test"), Storage.UNDEFINED);
        // we fool generics with mxproxy
        //noinspection RedundantCast
        c.save("test", new TestProxy());
        assertEquals(c.load("test"), "result");
        c.unlock("test");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testLocking() {
        MxDataSerializator serializator = mock(MxDataSerializator.class);
        final MxCachePoolManager<Value> manager = new MxCachePoolManager<Value>("test_cache", 0.0, 0.0, 0, MxConvertType.ONLY_DEFAULT,
                                                                                mock(MBeanRegistrator.class),
                                                                                TEST_RESOURCE_MANAGER, new MxCacheFlusher(),
                                                                                new MockConvertFromByteArray10<Value>(serializator),
                                                                                new MockConvertToByteArray10<Value>(serializator),
                                                                                new MockConvertFromResource10<Value>(serializator),
                                                                                new MockConvertToResource10<Value>(TEST_RESOURCE_MANAGER, serializator));
        MxPooledCacheStrategy<Value, MxConvertType> strategy = new TestStrategy<Value>(manager);
        // noinspection unchecked
        PooledCache<String, Value, MxConvertType> c = new PooledCache<String, Value, MxConvertType>(null, manager, MxConvertType.DEFAULT, strategy);

        c.lock("test");
        try {
            c.load("test2"); // <-- it fails here cause key "test2" is never locked
        } finally {
            c.unlock("test");
        }
    }

    private static void lockAndClear(ObjectObjectCache<String, Value> cache) {
        cache.getLock().lock();
        try {
            cache.clear();
        } finally {
            cache.getLock().unlock();
        }
    }

    private static class TestResourceManager implements MxResourceManager {
        @SuppressWarnings({ "unchecked" })
        @Override
        public <T extends MxResource> T getTempResource(String resourceName) {
            return (T) new TestResource();
        }

        private static class TestResource implements MxResource {
            private ByteArrayOutputStream bos;

            @Override
            public synchronized InputStream getInputStream() {
                if (bos == null) {
                    return null;
                }
                return new ByteArrayInputStream(bos.toByteArray());
            }

            @Override
            public boolean deleteOnExit() {
                return false;
            }

            @Override
            public synchronized OutputStream getOutputStream(boolean append) throws IOException {
                if (!append || bos == null) {
                    bos = new ByteArrayOutputStream();
                }
                return bos;
            }

            @Override
            public boolean exists() {
                return bos != null;
            }
        }
    }

    private static class TestStrategy<V> extends MxPooledCacheStrategy<V, MxConvertType> {
        private final MxCachePoolManager<V> manager;

        public TestStrategy(MxCachePoolManager<V> manager) {
            this.manager = manager;
        }

        @Override
        protected Confidence getConfidence(MxConvertType mxConvertType) {
            return new Confidence(1, 1);
        }

        @Override
        protected MxConvertType getElementType(CacheDescriptor<?> descriptor) {
            return MxConvertType.DEFAULT;
        }

        @Override
        protected MxCachePoolManager<V> getCacheManager(CacheDescriptor descriptor) {
            return manager;
        }

        @Override
        protected void checkDescriptor(CacheDescriptor descriptor) {
        }
    }

    private static class TestProxy extends MxProxy<String, ConstResolvable<String>> implements IValue {
        @NotNull
        @Override
        public ConstResolvable<String> getValue() {
            return new ConstResolvable<String>("result");
        }

        @Override
        public String get() {
            return getValue().getValue();
        }
    }

    public class MockConvertFromResource10<T> implements MxConverter<MxResource, T> {
        private final MxDataSerializator dataSerializator;

        public MockConvertFromResource10(MxDataSerializator dataSerializator) {
            this.dataSerializator = dataSerializator;
        }

        @Override
        public T convert(MxResource resource) {
            if (!resource.exists()) {
                throw new RuntimeException();
            }
            try {
                //noinspection RedundantTypeArguments
                return dataSerializator.<T>deserialize(new BufferedInputStream(resource.getInputStream()));
            } catch (Exception e) {
                throw new ConverterException(e);
            }
        }
    }

    public class MockConvertToResource10<T> extends MxAbstractResourceConverter<T> {
        private final MxDataSerializator dataSerializator;

        public MockConvertToResource10(MxResourceManager resourceManager, MxDataSerializator dataSerializator) {
            super(resourceManager);
            this.dataSerializator = dataSerializator;
        }

        @Override
        public MxResource convert(T t) {
            if (t == null) {
                return null;
            }
            MxResource res = createUniqueCacheFile(t.getClass());
            try {
                dataSerializator.serialize(t, new BufferedOutputStream(res.getOutputStream(false)));
                return res;
            } catch (Exception e) {
                throw new ConverterException(e);
            }
        }
    }

    public class MockConvertFromByteArray10<T> implements MxConverter<byte[], T> {
        private final MxDataSerializator dataSerializator;

        public MockConvertFromByteArray10(MxDataSerializator dataSerializator) {
            this.dataSerializator = dataSerializator;
        }

        @Override
        public T convert(byte[] bytes) {
            try {
                //noinspection RedundantTypeArguments
                return dataSerializator.<T>deserialize(bytes);
            } catch (IOException e) {
                throw new ConverterException(e);
            } catch (ClassNotFoundException e) {
                throw new ConverterException(e);
            }
        }
    }

    public class MockConvertToByteArray10<T> implements MxConverter<T, byte[]> {
        private final MxDataSerializator dataSerializator;

        public MockConvertToByteArray10(MxDataSerializator dataSerializator) {
            this.dataSerializator = dataSerializator;
        }

        @Override
        public byte[] convert(T t) {
            try {
                return dataSerializator.serialize(t);
            } catch (IOException e) {
                throw new ConverterException(e);
            }
        }
    }
}
