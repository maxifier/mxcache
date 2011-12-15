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
public class BooleanDoubleCacheBoxKeyTest {
    private static final BooleanDoubleCalculatable CALCULATABLE = new BooleanDoubleCalculatable() {
        @Override
        public double calculate(Object owner, boolean o) {
            assert o == true;
            return 42d;
        }
    };

    public void testMiss() {
        ObjectDoubleStorage storage = mock(ObjectDoubleStorage.class);

        when(storage.isCalculated(true)).thenReturn(false);
        when(storage.size()).thenReturn(0);

        BooleanDoubleCache cache = (BooleanDoubleCache) Wrapping.getFactory(new Signature(Object.class, double.class), new Signature(boolean.class, double.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true) == 42d;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated(true);
        verify(storage).save(true, 42d);
        verifyNoMoreInteractions(storage);
    }

    public void testHit() {
        ObjectDoubleStorage storage = mock(ObjectDoubleStorage.class);

        BooleanDoubleCache cache = (BooleanDoubleCache) Wrapping.getFactory(new Signature(Object.class, double.class), new Signature(boolean.class, double.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        when(storage.isCalculated(true)).thenReturn(true);
        when(storage.load(true)).thenReturn(42d);
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true) == 42d;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated(true);
        verify(storage).load(true);
        verifyNoMoreInteractions(storage);
    }

    public void testClear() {
        ObjectDoubleStorage storage = mock(ObjectDoubleStorage.class);

        BooleanDoubleCache cache = (BooleanDoubleCache) Wrapping.getFactory(new Signature(Object.class, double.class), new Signature(boolean.class, double.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        ObjectDoubleStorage storage = mock(ObjectDoubleStorage.class);

        when(storage.isCalculated(true)).thenReturn(false, true);
        when(storage.load(true)).thenReturn(42d);

        BooleanDoubleCalculatable calculatable = mock(BooleanDoubleCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", true)).thenThrow(new ResourceOccupied(r));

        BooleanDoubleCache cache = (BooleanDoubleCache) Wrapping.getFactory(new Signature(Object.class, double.class), new Signature(boolean.class, double.class), false).
                wrap("123", calculatable, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true) == 42d;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).isCalculated(true);
        verify(storage).load(true);
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", true);
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        ObjectDoubleStorage storage = mock(ObjectDoubleStorage.class);

        when(storage.isCalculated(true)).thenReturn(false);

        BooleanDoubleCache cache = (BooleanDoubleCache) Wrapping.getFactory(new Signature(Object.class, double.class), new Signature(boolean.class, double.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true) == 42d;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).isCalculated(true);
        verify(storage).save(true, 42d);
        verifyNoMoreInteractions(storage);
    }
}
