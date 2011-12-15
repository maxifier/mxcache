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
public class DoubleCacheBoxValueTest {
    private static final DoubleCalculatable CALCULATABLE = new DoubleCalculatable() {
        @Override
        public double calculate(Object owner) {
            return 42d;
        }
    };

    public void testMiss() {
        ObjectStorage storage = mock(ObjectStorage.class);

        when(storage.load()).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        DoubleCache cache = (DoubleCache) Wrapping.getFactory(new Signature(null, Object.class), new Signature(null, double.class), false).
                    wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate() == 42d;

        verify(storage).size();
        verify(storage, atLeast(1)).load();
        verify(storage).save(42d);
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;
    }

    public void testHit() {
        ObjectStorage storage = mock(ObjectStorage.class);

        DoubleCache cache = (DoubleCache) Wrapping.getFactory(new Signature(null, Object.class), new Signature(null, double.class), false).
                    wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        when(storage.load()).thenReturn(42d);
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate() == 42d;

        verify(storage).size();
        verify(storage, atLeast(1)).load();
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;
    }

    public void testClear() {
        ObjectStorage storage = mock(ObjectStorage.class);

        DoubleCache cache = (DoubleCache) Wrapping.getFactory(new Signature(null, Object.class), new Signature(null, double.class), false).
                    wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        ObjectStorage storage = mock(ObjectStorage.class);

        when(storage.load()).thenReturn(Storage.UNDEFINED, 42d);

        DoubleCalculatable calculatable = mock(DoubleCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123")).thenThrow(new ResourceOccupied(r));

        DoubleCache cache = (DoubleCache) Wrapping.getFactory(new Signature(null, Object.class), new Signature(null, double.class), false).
                    wrap("123", calculatable, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate() == 42d;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(2)).load();
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123");
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        ObjectStorage storage = mock(ObjectStorage.class);

        when(storage.load()).thenReturn(Storage.UNDEFINED);

        DoubleCache cache = (DoubleCache) Wrapping.getFactory(new Signature(null, Object.class), new Signature(null, double.class), false).
                    wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate() == 42d;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load();
        verify(storage).save(42d);
        verifyNoMoreInteractions(storage);
    }
}
