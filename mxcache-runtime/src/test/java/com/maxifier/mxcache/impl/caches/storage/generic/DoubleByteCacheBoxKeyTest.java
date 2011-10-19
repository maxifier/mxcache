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
public class DoubleByteCacheBoxKeyTest {
    private static final DoubleByteCalculatable CALCULATABLE = new DoubleByteCalculatable() {
        @Override
        public byte calculate(Object owner, double o) {
            assert o == 42d;
            return (byte)42;
        }
    };

    public void testMiss() {
        ObjectByteStorage storage = mock(ObjectByteStorage.class);

        when(storage.isCalculated(42d)).thenReturn(false);
        when(storage.size()).thenReturn(0);

        DoubleByteCache cache = (DoubleByteCache) Wrapping.getFactory(new Signature(Object.class, byte.class), new Signature(double.class, byte.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.size() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42d) == (byte)42;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated(42d);
        verify(storage).save(42d, (byte)42);
        verifyNoMoreInteractions(storage);
    }

    public void testHit() {
        ObjectByteStorage storage = mock(ObjectByteStorage.class);

        DoubleByteCache cache = (DoubleByteCache) Wrapping.getFactory(new Signature(Object.class, byte.class), new Signature(double.class, byte.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        when(storage.isCalculated(42d)).thenReturn(true);
        when(storage.load(42d)).thenReturn((byte)42);
        when(storage.size()).thenReturn(1);

        assert cache.size() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42d) == (byte)42;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated(42d);
        verify(storage).load(42d);
        verifyNoMoreInteractions(storage);
    }

    public void testClear() {
        ObjectByteStorage storage = mock(ObjectByteStorage.class);

        DoubleByteCache cache = (DoubleByteCache) Wrapping.getFactory(new Signature(Object.class, byte.class), new Signature(double.class, byte.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        ObjectByteStorage storage = mock(ObjectByteStorage.class);

        when(storage.isCalculated(42d)).thenReturn(false, true);
        when(storage.load(42d)).thenReturn((byte)42);

        DoubleByteCalculatable calculatable = mock(DoubleByteCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", 42d)).thenThrow(new ResourceOccupied(r));

        DoubleByteCache cache = (DoubleByteCache) Wrapping.getFactory(new Signature(Object.class, byte.class), new Signature(double.class, byte.class), false).
                wrap("123", calculatable, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42d) == (byte)42;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).isCalculated(42d);
        verify(storage).load(42d);
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", 42d);
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        ObjectByteStorage storage = mock(ObjectByteStorage.class);

        when(storage.isCalculated(42d)).thenReturn(false);

        DoubleByteCache cache = (DoubleByteCache) Wrapping.getFactory(new Signature(Object.class, byte.class), new Signature(double.class, byte.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42d) == (byte)42;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).isCalculated(42d);
        verify(storage).save(42d, (byte)42);
        verifyNoMoreInteractions(storage);
    }
}
