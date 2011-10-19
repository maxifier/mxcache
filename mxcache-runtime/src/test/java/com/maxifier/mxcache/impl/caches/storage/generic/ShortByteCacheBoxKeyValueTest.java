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
public class ShortByteCacheBoxKeyValueTest {
    private static final ShortByteCalculatable CALCULATABLE = new ShortByteCalculatable() {
        @Override
        public byte calculate(Object owner, short o) {
            assert o == (short)42;
            return (byte)42;
        }
    };

    public void testMiss() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        when(storage.load((short)42)).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        ShortByteCache cache = (ShortByteCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(short.class, byte.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.size() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((short)42) == (byte)42;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).load((short)42);
        verify(storage).save((short)42, (byte)42);
        verifyNoMoreInteractions(storage);
    }

    public void testHit() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        ShortByteCache cache = (ShortByteCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(short.class, byte.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        when(storage.load((short)42)).thenReturn((byte)42);
        when(storage.size()).thenReturn(1);

        assert cache.size() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((short)42) == (byte)42;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(2)).load((short)42);
        verifyNoMoreInteractions(storage);
    }

    public void testClear() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        ShortByteCache cache = (ShortByteCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(short.class, byte.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        when(storage.load((short)42)).thenReturn(Storage.UNDEFINED, (byte)42);

        ShortByteCalculatable calculatable = mock(ShortByteCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", (short)42)).thenThrow(new ResourceOccupied(r));

        ShortByteCache cache = (ShortByteCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(short.class, byte.class), false).
                wrap("123", calculatable, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((short)42) == (byte)42;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(2)).load((short)42);
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", (short)42);
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        when(storage.load((short)42)).thenReturn(Storage.UNDEFINED);

        ShortByteCache cache = (ShortByteCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(short.class, byte.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((short)42) == (byte)42;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load((short)42);
        verify(storage).save((short)42, (byte)42);
        verifyNoMoreInteractions(storage);
    }
}
