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
public class ByteLongCacheBoxValueTest {
    private static final ByteLongCalculatable CALCULATABLE = new ByteLongCalculatable() {
        @Override
        public long calculate(Object owner, byte o) {
            assert o == (byte)42;
            return 42L;
        }
    };

    public void testMiss() {
        ByteObjectStorage storage = mock(ByteObjectStorage.class);

        when(storage.load((byte)42)).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        ByteLongCache cache = (ByteLongCache) Wrapping.getFactory(new Signature(byte.class, Object.class), new Signature(byte.class, long.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42) == 42L;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).load((byte)42);
        verify(storage).save((byte)42, 42L);
        verifyNoMoreInteractions(storage);
    }

    public void testHit() {
        ByteObjectStorage storage = mock(ByteObjectStorage.class);

        ByteLongCache cache = (ByteLongCache) Wrapping.getFactory(new Signature(byte.class, Object.class), new Signature(byte.class, long.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        when(storage.load((byte)42)).thenReturn(42L);
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42) == 42L;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(2)).load((byte)42);
        verifyNoMoreInteractions(storage);
    }

    public void testClear() {
        ByteObjectStorage storage = mock(ByteObjectStorage.class);

        ByteLongCache cache = (ByteLongCache) Wrapping.getFactory(new Signature(byte.class, Object.class), new Signature(byte.class, long.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        ByteObjectStorage storage = mock(ByteObjectStorage.class);

        when(storage.load((byte)42)).thenReturn(Storage.UNDEFINED, 42L);

        ByteLongCalculatable calculatable = mock(ByteLongCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", (byte)42)).thenThrow(new ResourceOccupied(r));

        ByteLongCache cache = (ByteLongCache) Wrapping.getFactory(new Signature(byte.class, Object.class), new Signature(byte.class, long.class), false).
                wrap("123", calculatable, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42) == 42L;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(2)).load((byte)42);
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", (byte)42);
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        ByteObjectStorage storage = mock(ByteObjectStorage.class);

        when(storage.load((byte)42)).thenReturn(Storage.UNDEFINED);

        ByteLongCache cache = (ByteLongCache) Wrapping.getFactory(new Signature(byte.class, Object.class), new Signature(byte.class, long.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42) == 42L;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load((byte)42);
        verify(storage).save((byte)42, 42L);
        verifyNoMoreInteractions(storage);
    }
}
