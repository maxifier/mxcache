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
public class LongLongCacheBoxKeyTest {
    private static final LongLongCalculatable CALCULATABLE = new LongLongCalculatable() {
        @Override
        public long calculate(Object owner, long o) {
            assert o == 42L;
            return 42L;
        }
    };

    public void testMiss() {
        ObjectLongStorage storage = mock(ObjectLongStorage.class);

        when(storage.isCalculated(42L)).thenReturn(false);
        when(storage.size()).thenReturn(0);

        LongLongCache cache = (LongLongCache) Wrapping.getFactory(new Signature(Object.class, long.class), new Signature(long.class, long.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.size() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42L) == 42L;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated(42L);
        verify(storage).save(42L, 42L);
        verifyNoMoreInteractions(storage);
    }

    public void testHit() {
        ObjectLongStorage storage = mock(ObjectLongStorage.class);

        LongLongCache cache = (LongLongCache) Wrapping.getFactory(new Signature(Object.class, long.class), new Signature(long.class, long.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        when(storage.isCalculated(42L)).thenReturn(true);
        when(storage.load(42L)).thenReturn(42L);
        when(storage.size()).thenReturn(1);

        assert cache.size() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42L) == 42L;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated(42L);
        verify(storage).load(42L);
        verifyNoMoreInteractions(storage);
    }

    public void testClear() {
        ObjectLongStorage storage = mock(ObjectLongStorage.class);

        LongLongCache cache = (LongLongCache) Wrapping.getFactory(new Signature(Object.class, long.class), new Signature(long.class, long.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        ObjectLongStorage storage = mock(ObjectLongStorage.class);

        when(storage.isCalculated(42L)).thenReturn(false, true);
        when(storage.load(42L)).thenReturn(42L);

        LongLongCalculatable calculatable = mock(LongLongCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", 42L)).thenThrow(new ResourceOccupied(r));

        LongLongCache cache = (LongLongCache) Wrapping.getFactory(new Signature(Object.class, long.class), new Signature(long.class, long.class), false).
                wrap("123", calculatable, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42L) == 42L;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).isCalculated(42L);
        verify(storage).load(42L);
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", 42L);
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        ObjectLongStorage storage = mock(ObjectLongStorage.class);

        when(storage.isCalculated(42L)).thenReturn(false);

        LongLongCache cache = (LongLongCache) Wrapping.getFactory(new Signature(Object.class, long.class), new Signature(long.class, long.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42L) == 42L;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).isCalculated(42L);
        verify(storage).save(42L, 42L);
        verifyNoMoreInteractions(storage);
    }
}
