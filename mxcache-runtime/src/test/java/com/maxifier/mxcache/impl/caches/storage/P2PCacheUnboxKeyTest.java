/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.storage;

import com.maxifier.mxcache.CacheExceptionPolicy;
import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.GeneratorAdapter;
import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.caches.Calculable;
import com.maxifier.mxcache.caches.ObjectObjectCalculatable;
import com.maxifier.mxcache.exceptions.CacheExceptionHandler;
import com.maxifier.mxcache.exceptions.ExceptionRecord;
import com.maxifier.mxcache.impl.CacheId;
import com.maxifier.mxcache.impl.MutableStatisticsImpl;
import com.maxifier.mxcache.impl.resource.DependencyTracker;
import com.maxifier.mxcache.impl.resource.ResourceOccupied;
import com.maxifier.mxcache.impl.wrapping.Wrapping;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.CacheProvider;
import com.maxifier.mxcache.provider.Signature;
import com.maxifier.mxcache.resource.MxResource;
import com.maxifier.mxcache.storage.Storage;
import com.maxifier.mxcache.transform.ScalarTransformGenerator;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nullable;

import static com.maxifier.mxcache.impl.caches.storage.TestHelper.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/**
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class P2PCacheUnboxKeyTest {
    private static final Object[][] PRIMITIVES = {
            {boolean.class, true},
            {byte.class, (byte) 42},
            {short.class, (short) 43},
            {char.class, '*'},
            {int.class, 44},
            {long.class, 45L},
            {float.class, 46.5f},
            {double.class, 47.5},
            {Object.class, "123"}
    };
    public static final Signature BOXED_SIGNATURE = new Signature(Object.class, Object.class);

    @DataProvider
    public Object[][] getAllTypes() {
        Object[][] res = new Object[PRIMITIVES.length * (PRIMITIVES.length - 1)][];
        for (int i = 0; i < PRIMITIVES.length - 1; i++) {
            for (int j = 0; j < PRIMITIVES.length; j++) {
                res[i * PRIMITIVES.length + j] = new Object[]{PRIMITIVES[i][0], PRIMITIVES[j][0], PRIMITIVES[i][1], PRIMITIVES[j][1]};
            }
        }
        return res;
    }

    @Test(dataProvider = "getAllTypes")
    public <K, V> void testMiss(Class<K> keyType, final Class<V> valueType, final K key, final V value) throws Throwable {

        Signature signature = new Signature(keyType, valueType);
        Storage storage = mock(signature.getStorageInterface());
        Calculable calculable = mock(ObjectObjectCalculatable.class);
        when(calculate(calculable, key)).thenReturn(value);

        when(load(storage, key)).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);
        
        Cache cache = Wrapping.getFactory(signature, BOXED_SIGNATURE, null, fakeTransform(valueType), false).
                wrap("123", calculable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assertEquals(cache.getSize(), 0);
        assertEquals(cache.getStatistics().getHits(), 0);
        assertEquals(cache.getStatistics().getMisses(), 0);

        Assert.assertEquals(getOrCreate(cache, key), value);

        assertEquals(cache.getStatistics().getHits(), 0);
        assertEquals(cache.getStatistics().getMisses(), 1);

        verify(storage).size();
        load(verify(storage, atLeast(1)), key);
        save(verify(storage), key, value);
        verifyNoMoreInteractions(storage);
    }

    private <V> ScalarTransformGenerator fakeTransform(final Class<V> valueType) {
        return new ScalarTransformGenerator() {
            @Override
            public void generateForward(Type thisType, int fieldIndex, GeneratorAdapter method) {
                // do nothing, just box/unbox
            }

            @Override
            public void generateBackward(Type thisType, int fieldIndex, GeneratorAdapter method) {

            }

            @Override
            public Class<?> getInType() {
                return valueType;
            }

            @Nullable
            @Override
            public Class<?> getOutType() {
                return valueType;
            }
        };
    }

    @Test(dataProvider = "getAllTypes")
    public <K, V> void testExceptionTransparency(Class<K> keyType, Class<V> valueType, final K key, final V value) throws Throwable {
        CacheProvider provider = mock(CacheProvider.class);
        CacheDescriptor descriptor = mock(CacheDescriptor.class);
        CacheExceptionPolicy policy = mock(CacheExceptionPolicy.class);
        when(policy.rememberExceptions()).thenReturn(true);
        when(policy.retries()).thenReturn(0);
        when(policy.specialCases()).thenReturn(new CacheExceptionPolicy.SpecialCase[]{});
        when(descriptor.getExceptionHandler()).thenReturn(new CacheExceptionHandler(policy));
        when(provider.getDescriptor(any(CacheId.class))).thenReturn(descriptor);
        CacheFactory.setProviderOverride(provider);
        try {
            Signature signature = new Signature(keyType, valueType);
            Storage storage = mock(signature.getStorageInterface());
            Calculable calculable = mock(ObjectObjectCalculatable.class);
            RuntimeException e1 = new RuntimeException("1");
            when(calculate(calculable, key)).thenThrow(e1, new RuntimeException("2"), new RuntimeException("3"));

            when(load(storage, key)).thenReturn(Storage.UNDEFINED);
            when(storage.size()).thenReturn(0);

            Cache cache = Wrapping.getFactory(signature, BOXED_SIGNATURE, null, fakeTransform(valueType), false).
                    wrap("123", calculable, storage, new MutableStatisticsImpl());
            cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

            assertEquals(cache.getSize(), 0);
            assertEquals(cache.getStatistics().getHits(), 0);
            assertEquals(cache.getStatistics().getMisses(), 0);

            try {
                assertEquals(getOrCreate(cache, key), value);
            } catch (RuntimeException e) {
                assertEquals(e.getMessage(), "1");
            }

            assertEquals(cache.getStatistics().getHits(), 0);
            // exception is also a miss
            assertEquals(cache.getStatistics().getMisses(), 1);

            calculate(verify(calculable, times(1)), key);
            verifyNoMoreInteractions(calculable);

            verify(storage).size();
            load(verify(storage, atLeast(1)), key);
            save(verify(storage), key, new ExceptionRecord(e1, 0));
            verifyNoMoreInteractions(storage);
        } finally {
            CacheFactory.setProviderOverride(null);
        }
    }

    @Test(dataProvider = "getAllTypes")
    public <K, V> void testExceptionNoRemember(Class<K> keyType, Class<V> valueType, final K key, final V value) throws Throwable {
        CacheProvider provider = mock(CacheProvider.class);
        CacheDescriptor descriptor = mock(CacheDescriptor.class);
        CacheExceptionPolicy policy = mock(CacheExceptionPolicy.class);
        when(policy.rememberExceptions()).thenReturn(false);
        when(policy.retries()).thenReturn(0);
        when(policy.specialCases()).thenReturn(new CacheExceptionPolicy.SpecialCase[]{});
        when(descriptor.getExceptionHandler()).thenReturn(new CacheExceptionHandler(policy));
        when(provider.getDescriptor(any(CacheId.class))).thenReturn(descriptor);
        CacheFactory.setProviderOverride(provider);
        try {
            Signature signature = new Signature(keyType, valueType);
            Storage storage = mock(signature.getStorageInterface());
            Calculable calculable = mock(ObjectObjectCalculatable.class);
            when(calculate(calculable, key)).thenThrow(new RuntimeException("1"), new RuntimeException("2"), new RuntimeException("3"));

            when(load(storage, key)).thenReturn(Storage.UNDEFINED);
            when(storage.size()).thenReturn(0);

            Cache cache = Wrapping.getFactory(signature, BOXED_SIGNATURE, null, fakeTransform(valueType), false).
                    wrap("123", calculable, storage, new MutableStatisticsImpl());
            cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

            assertEquals(cache.getSize(), 0);
            assertEquals(cache.getStatistics().getHits(), 0);
            assertEquals(cache.getStatistics().getMisses(), 0);

            try {
                assertEquals(getOrCreate(cache, key), value);
            } catch (RuntimeException e) {
                assertEquals(e.getMessage(), "1");
            }
            try {
                assertEquals(getOrCreate(cache, key), value);
            } catch (RuntimeException e) {
                assertEquals(e.getMessage(), "2");
            }
            try {
                assertEquals(getOrCreate(cache, key), value);
            } catch (RuntimeException e) {
                assertEquals(e.getMessage(), "3");
            }

            assertEquals(cache.getStatistics().getHits(), 0);
            // exception is also a miss
            assertEquals(cache.getStatistics().getMisses(), 3);

            calculate(verify(calculable, times(3)), key);
            verifyNoMoreInteractions(calculable);

            verify(storage).size();
            load(verify(storage, atLeast(2)), key);
            verifyNoMoreInteractions(storage);
        } finally {
            CacheFactory.setProviderOverride(null);
        }
    }

    @Test(dataProvider = "getAllTypes")
    public <K, V> void testExceptionRetry(Class<K> keyType, Class<V> valueType, final K key, final V value) throws Throwable {
        CacheProvider provider = mock(CacheProvider.class);
        CacheDescriptor descriptor = mock(CacheDescriptor.class);
        CacheExceptionPolicy policy = mock(CacheExceptionPolicy.class);
        when(policy.rememberExceptions()).thenReturn(true);
        when(policy.retries()).thenReturn(1);
        when(policy.specialCases()).thenReturn(new CacheExceptionPolicy.SpecialCase[]{});
        when(descriptor.getExceptionHandler()).thenReturn(new CacheExceptionHandler(policy));
        when(provider.getDescriptor(any(CacheId.class))).thenReturn(descriptor);
        CacheFactory.setProviderOverride(provider);
        try {
            Signature signature = new Signature(keyType, valueType);
            Storage storage = mock(signature.getStorageInterface());
            Calculable calculable = mock(ObjectObjectCalculatable.class);
            RuntimeException e2 = new RuntimeException("2");
            when(calculate(calculable, key)).thenThrow(new RuntimeException("1"), e2, new RuntimeException("3"));

            when(load(storage, key)).thenReturn(Storage.UNDEFINED);
            when(storage.size()).thenReturn(0);

            Cache cache = Wrapping.getFactory(signature, BOXED_SIGNATURE, null, fakeTransform(valueType), false).
                    wrap("123", calculable, storage, new MutableStatisticsImpl());
            cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

            assertEquals(cache.getSize(), 0);
            assertEquals(cache.getStatistics().getHits(), 0);
            assertEquals(cache.getStatistics().getMisses(), 0);

            try {
                assertEquals(getOrCreate(cache, key), value);
            } catch (RuntimeException e) {
                assertEquals(e.getMessage(), "2");
            }

            assertEquals(cache.getStatistics().getHits(), 0);
            // exception is also a miss
            assertEquals(cache.getStatistics().getMisses(), 1);

            calculate(verify(calculable, times(2)), key);
            verifyNoMoreInteractions(calculable);

            verify(storage).size();
            load(verify(storage, atLeast(1)), key);
            save(verify(storage), key, new ExceptionRecord(e2, 0));
            verifyNoMoreInteractions(storage);
        } finally {
            CacheFactory.setProviderOverride(null);
        }
    }

    @Test(dataProvider = "getAllTypes")
    public <K, V> void testExceptionTransparency2(Class<K> keyType, Class<V> valueType, final K key, final V value) throws Throwable {
        CacheProvider provider = mock(CacheProvider.class);
        CacheDescriptor descriptor = mock(CacheDescriptor.class);
        CacheExceptionPolicy policy = mock(CacheExceptionPolicy.class);
        when(policy.rememberExceptions()).thenReturn(true);
        when(policy.retries()).thenReturn(0);
        when(policy.specialCases()).thenReturn(new CacheExceptionPolicy.SpecialCase[]{});
        when(descriptor.getExceptionHandler()).thenReturn(new CacheExceptionHandler(policy));
        when(provider.getDescriptor(any(CacheId.class))).thenReturn(descriptor);
        CacheFactory.setProviderOverride(provider);
        try {
            Signature signature = new Signature(keyType, valueType);
            Storage storage = mock(signature.getStorageInterface());
            Calculable calculable = mock(ObjectObjectCalculatable.class);
            when(calculate(calculable, key)).thenReturn(value);

            when(load(storage, key)).thenReturn(new ExceptionRecord(new RuntimeException("1"), 0));
            when(storage.size()).thenReturn(0);

            Cache cache = Wrapping.getFactory(signature, BOXED_SIGNATURE, null, fakeTransform(valueType), false).
                    wrap("123", calculable, storage, new MutableStatisticsImpl());
            cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

            assertEquals(cache.getSize(), 0);
            assertEquals(cache.getStatistics().getHits(), 0);
            assertEquals(cache.getStatistics().getMisses(), 0);

            try {
                assertEquals(getOrCreate(cache, key), value);
            } catch (RuntimeException e) {
                assertEquals(e.getMessage(), "1");
            }

            assertEquals(cache.getStatistics().getHits(), 1);
            assertEquals(cache.getStatistics().getMisses(), 0);

            verify(storage).size();
            load(verify(storage, atLeast(1)), key);
            verifyNoMoreInteractions(storage);
        } finally {
            CacheFactory.setProviderOverride(null);
        }
    }

    @Test(dataProvider = "getAllTypes")
    public <K, V> void testHit(Class<K> keyType, Class<V> valueType, final K key, final V value) throws Throwable {
        Signature signature = new Signature(keyType, Object.class);
        Calculable calculable = mock(ObjectObjectCalculatable.class);
        when(calculate(calculable, key)).thenReturn(value);
        Storage storage = mock(signature.getStorageInterface());

        Cache cache = Wrapping.getFactory(signature, BOXED_SIGNATURE, null, fakeTransform(valueType), false).
                wrap("123", calculable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(load(storage, key)).thenReturn(value);
        when(storage.size()).thenReturn(1);

        assertEquals(cache.getSize(), 1);
        assertEquals(cache.getStatistics().getHits(), 0);
        assertEquals(cache.getStatistics().getMisses(), 0);

        Assert.assertEquals(getOrCreate(cache, key), value);

        assertEquals(cache.getStatistics().getHits(), 1);
        assertEquals(cache.getStatistics().getMisses(), 0);

        verify(storage).size();
        load(verify(storage, atLeast(1)), key);
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "getAllTypes")
    public <K, V> void testClear(Class<K> keyType, Class<V> valueType, final K key, final V value) throws Throwable {
        Signature signature = new Signature(keyType, valueType);
        Calculable calculable = mock(ObjectObjectCalculatable.class);
        when(calculate(calculable, key)).thenReturn(value);
        Storage storage = mock(signature.getStorageInterface());

        Cache cache = Wrapping.getFactory(signature, BOXED_SIGNATURE, null, fakeTransform(valueType), false).
                wrap("123", calculable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.invalidate();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "getAllTypes")
    public <K, V> void testSetDuringDependencyNodeOperations(Class<K> keyType, Class<V> valueType, final K key, final V value) throws Throwable {
        Signature signature = new Signature(keyType, valueType);
        Calculable calculable = mock(ObjectObjectCalculatable.class);

        Storage storage = mock(signature.getStorageInterface());

        when(load(storage, key)).thenReturn(Storage.UNDEFINED, value);

        MxResource r = mock(MxResource.class);
        when(calculate(calculable, key)).thenThrow(new ResourceOccupied(r));

        Cache cache = Wrapping.getFactory(signature, BOXED_SIGNATURE, null, fakeTransform(valueType), false).
                wrap("123", calculable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assertEquals(cache.getStatistics().getHits(), 0);
        assertEquals(cache.getStatistics().getMisses(), 0);

        Assert.assertEquals(getOrCreate(cache, key), value);

        assertEquals(cache.getStatistics().getHits(), 1);
        // an invocation to calculable that throws ResourceOccupied is also considered a miss.
        assertEquals(cache.getStatistics().getMisses(), 1);

        load(verify(storage, times(2)), key);
        verifyNoMoreInteractions(storage);
        calculate(verify(calculable), key);
        verifyNoMoreInteractions(calculable);
    }

    @Test(dataProvider = "getAllTypes")
    public <K, V> void testResetStat(Class<K> keyType, Class<V> valueType, final K key, final V value) throws Throwable {
        Signature signature = new Signature(keyType, valueType);
        Calculable calculable = mock(ObjectObjectCalculatable.class);
        when(calculate(calculable, key)).thenReturn(value);

        Storage storage = mock(signature.getStorageInterface());

        when(load(storage, key)).thenReturn(Storage.UNDEFINED);

        Cache cache = Wrapping.getFactory(signature, BOXED_SIGNATURE, null, fakeTransform(valueType), false).
                wrap("123", calculable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assertEquals(cache.getStatistics().getHits(), 0);
        assertEquals(cache.getStatistics().getMisses(), 0);

        Assert.assertEquals(getOrCreate(cache, key), value);

        assertEquals(cache.getStatistics().getHits(), 0);
        assertEquals(cache.getStatistics().getMisses(), 1);

        cache.getStatistics().reset();

        assertEquals(cache.getStatistics().getHits(), 0);
        assertEquals(cache.getStatistics().getMisses(), 0);

        load(verify(storage, atLeast(1)), key);
        save(verify(storage), key, value);
        verifyNoMoreInteractions(storage);
    }
}
