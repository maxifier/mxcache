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
 * GENERATED FROM P2PBoxValueCacheTest.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class BooleanShortCacheBoxValueTest {
    private static final BooleanShortCalculatable CALCULATABLE = new BooleanShortCalculatable() {
        @Override
        public short calculate(Object owner, boolean o) {
            assert o == true;
            return (short)42;
        }
    };

    public void testMiss() {
        BooleanObjectStorage storage = mock(BooleanObjectStorage.class);

        when(storage.load(true)).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        BooleanShortCache cache = (BooleanShortCache) Wrapping.getFactory(new Signature(boolean.class, Object.class), new Signature(boolean.class, short.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true) == (short)42;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).load(true);
        verify(storage).save(true, (short)42);
        verifyNoMoreInteractions(storage);
    }

    public void testHit() {
        BooleanObjectStorage storage = mock(BooleanObjectStorage.class);

        BooleanShortCache cache = (BooleanShortCache) Wrapping.getFactory(new Signature(boolean.class, Object.class), new Signature(boolean.class, short.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(storage.load(true)).thenReturn((short)42);
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true) == (short)42;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(2)).load(true);
        verifyNoMoreInteractions(storage);
    }

    public void testClear() {
        BooleanObjectStorage storage = mock(BooleanObjectStorage.class);

        BooleanShortCache cache = (BooleanShortCache) Wrapping.getFactory(new Signature(boolean.class, Object.class), new Signature(boolean.class, short.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        BooleanObjectStorage storage = mock(BooleanObjectStorage.class);

        when(storage.load(true)).thenReturn(Storage.UNDEFINED, (short)42);

        BooleanShortCalculatable calculatable = mock(BooleanShortCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", true)).thenThrow(new ResourceOccupied(r));

        BooleanShortCache cache = (BooleanShortCache) Wrapping.getFactory(new Signature(boolean.class, Object.class), new Signature(boolean.class, short.class), false).
                wrap("123", calculatable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true) == (short)42;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(2)).load(true);
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", true);
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        BooleanObjectStorage storage = mock(BooleanObjectStorage.class);

        when(storage.load(true)).thenReturn(Storage.UNDEFINED);

        BooleanShortCache cache = (BooleanShortCache) Wrapping.getFactory(new Signature(boolean.class, Object.class), new Signature(boolean.class, short.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true) == (short)42;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load(true);
        verify(storage).save(true, (short)42);
        verifyNoMoreInteractions(storage);
    }
}
