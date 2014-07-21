/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.storage.generic;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.storage.*;
import com.maxifier.mxcache.resource.MxResource;
import com.maxifier.mxcache.impl.MutableStatisticsImpl;
import com.maxifier.mxcache.impl.resource.DependencyTracker;
import com.maxifier.mxcache.impl.resource.ResourceOccupied;
import org.testng.annotations.Test;

import com.maxifier.mxcache.provider.Signature;
import com.maxifier.mxcache.impl.wrapping.Wrapping;

import static org.mockito.Mockito.*;

/**
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PBoxKeyCacheTest.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class ShortLongCacheBoxKeyTest {
    private static final ShortLongCalculatable CALCULATABLE = new ShortLongCalculatable() {
        @Override
        public long calculate(Object owner, short o) {
            assert o == (short)42;
            return 42L;
        }
    };

    public void testMiss() {
        ObjectLongStorage storage = mock(ObjectLongStorage.class);

        when(storage.isCalculated((short)42)).thenReturn(false);
        when(storage.size()).thenReturn(0);

        ShortLongCache cache = (ShortLongCache) Wrapping.getFactory(new Signature(Object.class, long.class), new Signature(short.class, long.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((short)42) == 42L;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated((short)42);
        verify(storage).save((short)42, 42L);
        verifyNoMoreInteractions(storage);
    }

    public void testHit() {
        ObjectLongStorage storage = mock(ObjectLongStorage.class);

        ShortLongCache cache = (ShortLongCache) Wrapping.getFactory(new Signature(Object.class, long.class), new Signature(short.class, long.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(storage.isCalculated((short)42)).thenReturn(true);
        when(storage.load((short)42)).thenReturn(42L);
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((short)42) == 42L;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated((short)42);
        verify(storage).load((short)42);
        verifyNoMoreInteractions(storage);
    }

    public void testClear() {
        ObjectLongStorage storage = mock(ObjectLongStorage.class);

        ShortLongCache cache = (ShortLongCache) Wrapping.getFactory(new Signature(Object.class, long.class), new Signature(short.class, long.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        ObjectLongStorage storage = mock(ObjectLongStorage.class);

        when(storage.isCalculated((short)42)).thenReturn(false, true);
        when(storage.load((short)42)).thenReturn(42L);

        ShortLongCalculatable calculatable = mock(ShortLongCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", (short)42)).thenThrow(new ResourceOccupied(r));

        ShortLongCache cache = (ShortLongCache) Wrapping.getFactory(new Signature(Object.class, long.class), new Signature(short.class, long.class), false).
                wrap("123", calculatable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((short)42) == 42L;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).isCalculated((short)42);
        verify(storage).load((short)42);
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", (short)42);
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        ObjectLongStorage storage = mock(ObjectLongStorage.class);

        when(storage.isCalculated((short)42)).thenReturn(false);

        ShortLongCache cache = (ShortLongCache) Wrapping.getFactory(new Signature(Object.class, long.class), new Signature(short.class, long.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((short)42) == 42L;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).isCalculated((short)42);
        verify(storage).save((short)42, 42L);
        verifyNoMoreInteractions(storage);
    }
}
