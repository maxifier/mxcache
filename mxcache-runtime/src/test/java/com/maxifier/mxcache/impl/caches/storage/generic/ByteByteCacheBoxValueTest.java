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
public class ByteByteCacheBoxValueTest {
    private static final ByteByteCalculatable CALCULATABLE = new ByteByteCalculatable() {
        @Override
        public byte calculate(Object owner, byte o) {
            assert o == (byte)42;
            return (byte)42;
        }
    };

    public void testMiss() {
        ByteObjectStorage storage = mock(ByteObjectStorage.class);

        when(storage.load((byte)42)).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        ByteByteCache cache = (ByteByteCache) Wrapping.getFactory(new Signature(byte.class, Object.class), new Signature(byte.class, byte.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42) == (byte)42;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).load((byte)42);
        verify(storage).save((byte)42, (byte)42);
        verifyNoMoreInteractions(storage);
    }

    public void testHit() {
        ByteObjectStorage storage = mock(ByteObjectStorage.class);

        ByteByteCache cache = (ByteByteCache) Wrapping.getFactory(new Signature(byte.class, Object.class), new Signature(byte.class, byte.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(storage.load((byte)42)).thenReturn((byte)42);
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42) == (byte)42;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(2)).load((byte)42);
        verifyNoMoreInteractions(storage);
    }

    public void testClear() {
        ByteObjectStorage storage = mock(ByteObjectStorage.class);

        ByteByteCache cache = (ByteByteCache) Wrapping.getFactory(new Signature(byte.class, Object.class), new Signature(byte.class, byte.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        ByteObjectStorage storage = mock(ByteObjectStorage.class);

        when(storage.load((byte)42)).thenReturn(Storage.UNDEFINED, (byte)42);

        ByteByteCalculatable calculatable = mock(ByteByteCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", (byte)42)).thenThrow(new ResourceOccupied(r));

        ByteByteCache cache = (ByteByteCache) Wrapping.getFactory(new Signature(byte.class, Object.class), new Signature(byte.class, byte.class), false).
                wrap("123", calculatable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42) == (byte)42;

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

        ByteByteCache cache = (ByteByteCache) Wrapping.getFactory(new Signature(byte.class, Object.class), new Signature(byte.class, byte.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42) == (byte)42;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load((byte)42);
        verify(storage).save((byte)42, (byte)42);
        verifyNoMoreInteractions(storage);
    }
}
