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
public class ByteShortCacheBoxKeyTest {
    private static final ByteShortCalculatable CALCULATABLE = new ByteShortCalculatable() {
        @Override
        public short calculate(Object owner, byte o) {
            assert o == (byte)42;
            return (short)42;
        }
    };

    public void testMiss() {
        ObjectShortStorage storage = mock(ObjectShortStorage.class);

        when(storage.isCalculated((byte)42)).thenReturn(false);
        when(storage.size()).thenReturn(0);

        ByteShortCache cache = (ByteShortCache) Wrapping.getFactory(new Signature(Object.class, short.class), new Signature(byte.class, short.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42) == (short)42;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated((byte)42);
        verify(storage).save((byte)42, (short)42);
        verifyNoMoreInteractions(storage);
    }

    public void testHit() {
        ObjectShortStorage storage = mock(ObjectShortStorage.class);

        ByteShortCache cache = (ByteShortCache) Wrapping.getFactory(new Signature(Object.class, short.class), new Signature(byte.class, short.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        when(storage.isCalculated((byte)42)).thenReturn(true);
        when(storage.load((byte)42)).thenReturn((short)42);
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42) == (short)42;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated((byte)42);
        verify(storage).load((byte)42);
        verifyNoMoreInteractions(storage);
    }

    public void testClear() {
        ObjectShortStorage storage = mock(ObjectShortStorage.class);

        ByteShortCache cache = (ByteShortCache) Wrapping.getFactory(new Signature(Object.class, short.class), new Signature(byte.class, short.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        ObjectShortStorage storage = mock(ObjectShortStorage.class);

        when(storage.isCalculated((byte)42)).thenReturn(false, true);
        when(storage.load((byte)42)).thenReturn((short)42);

        ByteShortCalculatable calculatable = mock(ByteShortCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", (byte)42)).thenThrow(new ResourceOccupied(r));

        ByteShortCache cache = (ByteShortCache) Wrapping.getFactory(new Signature(Object.class, short.class), new Signature(byte.class, short.class), false).
                wrap("123", calculatable, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42) == (short)42;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).isCalculated((byte)42);
        verify(storage).load((byte)42);
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", (byte)42);
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        ObjectShortStorage storage = mock(ObjectShortStorage.class);

        when(storage.isCalculated((byte)42)).thenReturn(false);

        ByteShortCache cache = (ByteShortCache) Wrapping.getFactory(new Signature(Object.class, short.class), new Signature(byte.class, short.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42) == (short)42;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).isCalculated((byte)42);
        verify(storage).save((byte)42, (short)42);
        verifyNoMoreInteractions(storage);
    }
}
