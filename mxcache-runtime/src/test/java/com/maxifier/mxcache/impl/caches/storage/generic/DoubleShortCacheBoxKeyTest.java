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
public class DoubleShortCacheBoxKeyTest {
    private static final DoubleShortCalculatable CALCULATABLE = new DoubleShortCalculatable() {
        @Override
        public short calculate(Object owner, double o) {
            assert o == 42d;
            return (short)42;
        }
    };

    public void testMiss() {
        ObjectShortStorage storage = mock(ObjectShortStorage.class);

        when(storage.isCalculated(42d)).thenReturn(false);
        when(storage.size()).thenReturn(0);

        DoubleShortCache cache = (DoubleShortCache) Wrapping.getFactory(new Signature(Object.class, short.class), new Signature(double.class, short.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42d) == (short)42;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated(42d);
        verify(storage).save(42d, (short)42);
        verifyNoMoreInteractions(storage);
    }

    public void testHit() {
        ObjectShortStorage storage = mock(ObjectShortStorage.class);

        DoubleShortCache cache = (DoubleShortCache) Wrapping.getFactory(new Signature(Object.class, short.class), new Signature(double.class, short.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        when(storage.isCalculated(42d)).thenReturn(true);
        when(storage.load(42d)).thenReturn((short)42);
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42d) == (short)42;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated(42d);
        verify(storage).load(42d);
        verifyNoMoreInteractions(storage);
    }

    public void testClear() {
        ObjectShortStorage storage = mock(ObjectShortStorage.class);

        DoubleShortCache cache = (DoubleShortCache) Wrapping.getFactory(new Signature(Object.class, short.class), new Signature(double.class, short.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        ObjectShortStorage storage = mock(ObjectShortStorage.class);

        when(storage.isCalculated(42d)).thenReturn(false, true);
        when(storage.load(42d)).thenReturn((short)42);

        DoubleShortCalculatable calculatable = mock(DoubleShortCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", 42d)).thenThrow(new ResourceOccupied(r));

        DoubleShortCache cache = (DoubleShortCache) Wrapping.getFactory(new Signature(Object.class, short.class), new Signature(double.class, short.class), false).
                wrap("123", calculatable, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42d) == (short)42;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).isCalculated(42d);
        verify(storage).load(42d);
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", 42d);
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        ObjectShortStorage storage = mock(ObjectShortStorage.class);

        when(storage.isCalculated(42d)).thenReturn(false);

        DoubleShortCache cache = (DoubleShortCache) Wrapping.getFactory(new Signature(Object.class, short.class), new Signature(double.class, short.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42d) == (short)42;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).isCalculated(42d);
        verify(storage).save(42d, (short)42);
        verifyNoMoreInteractions(storage);
    }
}
