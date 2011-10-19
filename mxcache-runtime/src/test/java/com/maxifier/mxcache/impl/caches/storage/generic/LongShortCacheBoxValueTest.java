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
public class LongShortCacheBoxValueTest {
    private static final LongShortCalculatable CALCULATABLE = new LongShortCalculatable() {
        @Override
        public short calculate(Object owner, long o) {
            assert o == 42L;
            return (short)42;
        }
    };

    public void testMiss() {
        LongObjectStorage storage = mock(LongObjectStorage.class);

        when(storage.load(42L)).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        LongShortCache cache = (LongShortCache) Wrapping.getFactory(new Signature(long.class, Object.class), new Signature(long.class, short.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.size() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42L) == (short)42;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).load(42L);
        verify(storage).save(42L, (short)42);
        verifyNoMoreInteractions(storage);
    }

    public void testHit() {
        LongObjectStorage storage = mock(LongObjectStorage.class);

        LongShortCache cache = (LongShortCache) Wrapping.getFactory(new Signature(long.class, Object.class), new Signature(long.class, short.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        when(storage.load(42L)).thenReturn((short)42);
        when(storage.size()).thenReturn(1);

        assert cache.size() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42L) == (short)42;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(2)).load(42L);
        verifyNoMoreInteractions(storage);
    }

    public void testClear() {
        LongObjectStorage storage = mock(LongObjectStorage.class);

        LongShortCache cache = (LongShortCache) Wrapping.getFactory(new Signature(long.class, Object.class), new Signature(long.class, short.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        LongObjectStorage storage = mock(LongObjectStorage.class);

        when(storage.load(42L)).thenReturn(Storage.UNDEFINED, (short)42);

        LongShortCalculatable calculatable = mock(LongShortCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", 42L)).thenThrow(new ResourceOccupied(r));

        LongShortCache cache = (LongShortCache) Wrapping.getFactory(new Signature(long.class, Object.class), new Signature(long.class, short.class), false).
                wrap("123", calculatable, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42L) == (short)42;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(2)).load(42L);
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", 42L);
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        LongObjectStorage storage = mock(LongObjectStorage.class);

        when(storage.load(42L)).thenReturn(Storage.UNDEFINED);

        LongShortCache cache = (LongShortCache) Wrapping.getFactory(new Signature(long.class, Object.class), new Signature(long.class, short.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42L) == (short)42;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load(42L);
        verify(storage).save(42L, (short)42);
        verifyNoMoreInteractions(storage);
    }
}
