/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.storage.generic;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.storage.*;
import org.testng.annotations.Test;
import com.maxifier.mxcache.resource.MxResource;
import com.maxifier.mxcache.impl.MutableStatisticsImpl;
import com.maxifier.mxcache.impl.resource.DependencyTracker;
import com.maxifier.mxcache.impl.resource.ResourceOccupied;
import static org.testng.Assert.*;

import com.maxifier.mxcache.provider.Signature;
import com.maxifier.mxcache.impl.wrapping.Wrapping;

import static org.mockito.Mockito.*;

/**
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM PUnboxValueCacheTest.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class BooleanCacheUnboxValueTest {
    private static final ObjectCalculatable CALCULATABLE = new ObjectCalculatable() {
        @Override
        public Boolean calculate(Object owner) {
            return true;
        }
    };

    public void testMiss() {
        BooleanStorage storage = mock(BooleanStorage.class);

        when(storage.isCalculated()).thenReturn(false);
        when(storage.size()).thenReturn(0);

        ObjectCache cache = (ObjectCache) Wrapping.getFactory(new Signature(null, boolean.class), new Signature(null, Object.class), false).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assertEquals(cache.getOrCreate(), true);

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated();
        verify(storage).save(true);
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;
    }

    public void testHit() {
        BooleanStorage storage = mock(BooleanStorage.class);

        ObjectCache cache = (ObjectCache) Wrapping.getFactory(new Signature(null, boolean.class), new Signature(null, Object.class), false).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(storage.isCalculated()).thenReturn(true);
        when(storage.load()).thenReturn(true);
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assertEquals(cache.getOrCreate(), true);

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated();
        verify(storage).load();
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;
    }

    public void testClear() {
        BooleanStorage storage = mock(BooleanStorage.class);

        ObjectCache cache = (ObjectCache) Wrapping.getFactory(new Signature(null, boolean.class), new Signature(null, Object.class), false).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        BooleanStorage storage = mock(BooleanStorage.class);

        when(storage.isCalculated()).thenReturn(false, true);
        when(storage.load()).thenReturn(true);

        ObjectCalculatable calculatable = mock(ObjectCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123")).thenThrow(new ResourceOccupied(r));

        ObjectCache cache = (ObjectCache) Wrapping.getFactory(new Signature(null, boolean.class), new Signature(null, Object.class), false).
                    wrap("123", calculatable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assertEquals(cache.getOrCreate(), true);

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).isCalculated();
        verify(storage).load();
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123");
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        BooleanStorage storage = mock(BooleanStorage.class);

        when(storage.isCalculated()).thenReturn(false);

        ObjectCache cache = (ObjectCache) Wrapping.getFactory(new Signature(null, boolean.class), new Signature(null, Object.class), false).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assertEquals(cache.getOrCreate(), true);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).isCalculated();
        verify(storage).save(true);
        verifyNoMoreInteractions(storage);
    }
}
