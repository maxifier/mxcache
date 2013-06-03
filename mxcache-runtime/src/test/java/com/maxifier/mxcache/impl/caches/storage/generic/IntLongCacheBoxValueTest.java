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
public class IntLongCacheBoxValueTest {
    private static final IntLongCalculatable CALCULATABLE = new IntLongCalculatable() {
        @Override
        public long calculate(Object owner, int o) {
            assert o == 42;
            return 42L;
        }
    };

    public void testMiss() {
        IntObjectStorage storage = mock(IntObjectStorage.class);

        when(storage.load(42)).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        IntLongCache cache = (IntLongCache) Wrapping.getFactory(new Signature(int.class, Object.class), new Signature(int.class, long.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42) == 42L;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).load(42);
        verify(storage).save(42, 42L);
        verifyNoMoreInteractions(storage);
    }

    public void testHit() {
        IntObjectStorage storage = mock(IntObjectStorage.class);

        IntLongCache cache = (IntLongCache) Wrapping.getFactory(new Signature(int.class, Object.class), new Signature(int.class, long.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(storage.load(42)).thenReturn(42L);
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42) == 42L;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(2)).load(42);
        verifyNoMoreInteractions(storage);
    }

    public void testClear() {
        IntObjectStorage storage = mock(IntObjectStorage.class);

        IntLongCache cache = (IntLongCache) Wrapping.getFactory(new Signature(int.class, Object.class), new Signature(int.class, long.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        IntObjectStorage storage = mock(IntObjectStorage.class);

        when(storage.load(42)).thenReturn(Storage.UNDEFINED, 42L);

        IntLongCalculatable calculatable = mock(IntLongCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", 42)).thenThrow(new ResourceOccupied(r));

        IntLongCache cache = (IntLongCache) Wrapping.getFactory(new Signature(int.class, Object.class), new Signature(int.class, long.class), false).
                wrap("123", calculatable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42) == 42L;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(2)).load(42);
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", 42);
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        IntObjectStorage storage = mock(IntObjectStorage.class);

        when(storage.load(42)).thenReturn(Storage.UNDEFINED);

        IntLongCache cache = (IntLongCache) Wrapping.getFactory(new Signature(int.class, Object.class), new Signature(int.class, long.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42) == 42L;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load(42);
        verify(storage).save(42, 42L);
        verifyNoMoreInteractions(storage);
    }
}
