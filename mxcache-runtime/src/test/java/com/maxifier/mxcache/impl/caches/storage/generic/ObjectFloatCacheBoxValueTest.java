package com.maxifier.mxcache.impl.caches.storage.generic;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.storage.*;
import com.maxifier.mxcache.resource.MxResource;
import com.maxifier.mxcache.impl.MutableStatisticsImpl;
import com.maxifier.mxcache.impl.resource.DependencyTracker;
import com.maxifier.mxcache.impl.resource.ResourceOccupied;
import org.testng.annotations.Test;

import com.maxifier.mxcache.provider.Signature;
import com.maxifier.mxcache.impl.caches.storage.Wrapping;

import static org.mockito.Mockito.*;

/**
 * Project: Maxifier
 * Created by: Yakoushin Andrey
 * Date: 15.02.2010
 * Time: 13:40:10
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
 */
@Test
public class ObjectFloatCacheBoxValueTest {
    private static final ObjectFloatCalculatable CALCULATABLE = new ObjectFloatCalculatable() {
        @Override
        public float calculate(Object owner, Object o) {
            assert o == "123";
            return 42f;
        }
    };

    public void testMiss() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        when(storage.load("123")).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        ObjectFloatCache cache = (ObjectFloatCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(Object.class, float.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate("123") == 42f;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).load("123");
        verify(storage).save("123", 42f);
        verifyNoMoreInteractions(storage);
    }

    public void testHit() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        ObjectFloatCache cache = (ObjectFloatCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(Object.class, float.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        when(storage.load("123")).thenReturn(42f);
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate("123") == 42f;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(2)).load("123");
        verifyNoMoreInteractions(storage);
    }

    public void testClear() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        ObjectFloatCache cache = (ObjectFloatCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(Object.class, float.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        when(storage.load("123")).thenReturn(Storage.UNDEFINED, 42f);

        ObjectFloatCalculatable calculatable = mock(ObjectFloatCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", "123")).thenThrow(new ResourceOccupied(r));

        ObjectFloatCache cache = (ObjectFloatCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(Object.class, float.class), false).
                wrap("123", calculatable, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate("123") == 42f;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(2)).load("123");
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", "123");
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        when(storage.load("123")).thenReturn(Storage.UNDEFINED);

        ObjectFloatCache cache = (ObjectFloatCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(Object.class, float.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate("123") == 42f;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load("123");
        verify(storage).save("123", 42f);
        verifyNoMoreInteractions(storage);
    }
}
