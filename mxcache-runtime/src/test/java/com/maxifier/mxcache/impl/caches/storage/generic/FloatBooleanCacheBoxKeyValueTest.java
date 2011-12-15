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
public class FloatBooleanCacheBoxKeyValueTest {
    private static final FloatBooleanCalculatable CALCULATABLE = new FloatBooleanCalculatable() {
        @Override
        public boolean calculate(Object owner, float o) {
            assert o == 42f;
            return true;
        }
    };

    public void testMiss() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        when(storage.load(42f)).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        FloatBooleanCache cache = (FloatBooleanCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(float.class, boolean.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42f) == true;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).load(42f);
        verify(storage).save(42f, true);
        verifyNoMoreInteractions(storage);
    }

    public void testHit() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        FloatBooleanCache cache = (FloatBooleanCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(float.class, boolean.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        when(storage.load(42f)).thenReturn(true);
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42f) == true;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(2)).load(42f);
        verifyNoMoreInteractions(storage);
    }

    public void testClear() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        FloatBooleanCache cache = (FloatBooleanCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(float.class, boolean.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        when(storage.load(42f)).thenReturn(Storage.UNDEFINED, true);

        FloatBooleanCalculatable calculatable = mock(FloatBooleanCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", 42f)).thenThrow(new ResourceOccupied(r));

        FloatBooleanCache cache = (FloatBooleanCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(float.class, boolean.class), false).
                wrap("123", calculatable, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42f) == true;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(2)).load(42f);
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", 42f);
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        when(storage.load(42f)).thenReturn(Storage.UNDEFINED);

        FloatBooleanCache cache = (FloatBooleanCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(float.class, boolean.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42f) == true;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load(42f);
        verify(storage).save(42f, true);
        verifyNoMoreInteractions(storage);
    }
}
