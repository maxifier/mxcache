/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.storage;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.storage.*;
import com.maxifier.mxcache.resource.MxResource;
import com.maxifier.mxcache.impl.MutableStatisticsImpl;
import com.maxifier.mxcache.impl.resource.DependencyTracker;
import com.maxifier.mxcache.impl.resource.ResourceOccupied;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.maxifier.mxcache.provider.Signature;
import com.maxifier.mxcache.impl.wrapping.Wrapping;

import static com.maxifier.mxcache.impl.caches.storage.TestHelper.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/**
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class P2PCacheBoxKeyTest {
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
    public static final Signature BOXED_STORAGE = new Signature(Object.class, Object.class);

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
    public <K, V> void testMiss(Class<K> keyType, Class<V> valueType, final K key, final V value) throws Throwable {
        Signature signature = new Signature(keyType, valueType);
        Storage storage = mock(ObjectObjectStorage.class);
        Calculable calculable = mock(signature.getCalculableInterface());
        when(calculate(calculable, key)).thenReturn(value);

        when(load(storage, key)).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        Cache cache = Wrapping.getFactory(BOXED_STORAGE, signature, false).
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

    @Test(dataProvider = "getAllTypes")
    public <K, V> void testHit(Class<K> keyType, Class<V> valueType, final K key, final V value) throws Throwable {
        Signature signature = new Signature(keyType, valueType);
        Calculable calculable = mock(signature.getCalculableInterface());
        when(calculate(calculable, key)).thenReturn(value);
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        Cache cache = Wrapping.getFactory(BOXED_STORAGE, signature, false).
                wrap("123", calculable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(storage.load(key)).thenReturn(value);
        when(storage.size()).thenReturn(1);

        assertEquals(cache.getSize(), 1);
        assertEquals(cache.getStatistics().getHits(), 0);
        assertEquals(cache.getStatistics().getMisses(), 0);

        Assert.assertEquals(getOrCreate(cache, key), value);

        assertEquals(cache.getStatistics().getHits(), 1);
        assertEquals(cache.getStatistics().getMisses(), 0);

        verify(storage).size();
        verify(storage, atLeast(1)).load(key);
        verify(storage).load(key);
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "getAllTypes")
    public <K, V> void testClear(Class<K> keyType, Class<V> valueType, final K key, final V value) throws Throwable {
        Signature signature = new Signature(keyType, valueType);
        Calculable calculable = mock(signature.getCalculableInterface());
        when(calculate(calculable, key)).thenReturn(value);
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        Cache cache = Wrapping.getFactory(BOXED_STORAGE, signature, false).
                wrap("123", calculable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "getAllTypes")
    public <K, V> void testSetDuringDependencyNodeOperations(Class<K> keyType, Class<V> valueType, final K key, final V value) throws Throwable {
        Signature signature = new Signature(keyType, valueType);
        Calculable calculable = mock(signature.getCalculableInterface());

        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        when(storage.load(key)).thenReturn(Storage.UNDEFINED, value);

        MxResource r = mock(MxResource.class);
        when(calculate(calculable, key)).thenThrow(new ResourceOccupied(r));

        Cache cache = Wrapping.getFactory(BOXED_STORAGE, signature, false).
                wrap("123", calculable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assertEquals(cache.getStatistics().getHits(), 0);
        assertEquals(cache.getStatistics().getMisses(), 0);

        Assert.assertEquals(getOrCreate(cache, key), value);

        assertEquals(cache.getStatistics().getHits(), 1);
        assertEquals(cache.getStatistics().getMisses(), 0);

        verify(storage, times(2)).load(key);
        verifyNoMoreInteractions(storage);
        calculate(verify(calculable), key);
        verifyNoMoreInteractions(calculable);
    }

    @Test(dataProvider = "getAllTypes")
    public <K, V> void testResetStat(Class<K> keyType, Class<V> valueType, final K key, final V value) throws Throwable {
        Signature signature = new Signature(keyType, valueType);
        Calculable calculable = mock(signature.getCalculableInterface());
        when(calculate(calculable, key)).thenReturn(value);

        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        when(storage.load(key)).thenReturn(Storage.UNDEFINED);

        Cache cache = Wrapping.getFactory(BOXED_STORAGE, signature, false).
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

        verify(storage, atLeast(1)).load(key);
        verify(storage).save(key, value);
        verifyNoMoreInteractions(storage);
    }
}
