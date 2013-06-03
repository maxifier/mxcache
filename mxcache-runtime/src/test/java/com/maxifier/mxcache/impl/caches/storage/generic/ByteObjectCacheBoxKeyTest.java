package com.maxifier.mxcache.impl.caches.storage.generic;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.storage.*;
import com.maxifier.mxcache.resource.MxResource;
import com.maxifier.mxcache.impl.MutableStatisticsImpl;
import com.maxifier.mxcache.impl.resource.DependencyTracker;
import com.maxifier.mxcache.impl.resource.ResourceOccupied;
import com.maxifier.mxcache.interfaces.StatisticsHolder;
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
public class ByteObjectCacheBoxKeyTest {
    private static final ByteObjectCalculatable CALCULATABLE = new ByteObjectCalculatable() {
        @Override
        public Object calculate(Object owner, byte o) {
            assert o == (byte)42;
            return "123";
        }
    };

    public void testMiss() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        when(storage.load((byte)42)).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        ByteObjectCache cache = (ByteObjectCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(byte.class, Object.class), false).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42).equals("123");

        verify(storage).size();
        verify(storage, atLeast(1)).load((byte)42);
        verify(storage).save((byte)42, "123");
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;
    }

    public void testHit() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        ByteObjectCache cache = (ByteObjectCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(byte.class, Object.class), false).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(storage.load((byte)42)).thenReturn("123");
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42).equals("123");

        verify(storage).size();
        verify(storage, atLeast(1)).load((byte)42);
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;
    }

    public void testClear() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        ByteObjectCache cache = (ByteObjectCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(byte.class, Object.class), false).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        when(storage.load((byte)42)).thenReturn(Storage.UNDEFINED, "123");

        ByteObjectCalculatable calculatable = mock(ByteObjectCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", (byte)42)).thenThrow(new ResourceOccupied(r));

        ByteObjectCache cache = (ByteObjectCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(byte.class, Object.class), false).
                    wrap("123", calculatable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42).equals("123");

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).load((byte)42);
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", (byte)42);
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        when(storage.load((byte)42)).thenReturn(Storage.UNDEFINED);

        ByteObjectCache cache = (ByteObjectCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(byte.class, Object.class), false).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42).equals("123");;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load((byte)42);
        verify(storage).save((byte)42, "123");
        verifyNoMoreInteractions(storage);
    }

    public void testTransparentStat() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class, withSettings().extraInterfaces(StatisticsHolder.class));

        ByteObjectCache cache = (ByteObjectCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(byte.class, Object.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.getStatistics();

        verify((StatisticsHolder)storage).getStatistics();
        verifyNoMoreInteractions(storage);
    }
}
