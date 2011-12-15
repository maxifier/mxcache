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
public class BooleanIntCacheBoxKeyValueTest {
    private static final BooleanIntCalculatable CALCULATABLE = new BooleanIntCalculatable() {
        @Override
        public int calculate(Object owner, boolean o) {
            assert o == true;
            return 42;
        }
    };

    public void testMiss() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        when(storage.load(true)).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        BooleanIntCache cache = (BooleanIntCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(boolean.class, int.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true) == 42;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).load(true);
        verify(storage).save(true, 42);
        verifyNoMoreInteractions(storage);
    }

    public void testHit() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        BooleanIntCache cache = (BooleanIntCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(boolean.class, int.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        when(storage.load(true)).thenReturn(42);
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true) == 42;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(2)).load(true);
        verifyNoMoreInteractions(storage);
    }

    public void testClear() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        BooleanIntCache cache = (BooleanIntCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(boolean.class, int.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        when(storage.load(true)).thenReturn(Storage.UNDEFINED, 42);

        BooleanIntCalculatable calculatable = mock(BooleanIntCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", true)).thenThrow(new ResourceOccupied(r));

        BooleanIntCache cache = (BooleanIntCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(boolean.class, int.class), false).
                wrap("123", calculatable, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true) == 42;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(2)).load(true);
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", true);
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        ObjectObjectStorage storage = mock(ObjectObjectStorage.class);

        when(storage.load(true)).thenReturn(Storage.UNDEFINED);

        BooleanIntCache cache = (BooleanIntCache) Wrapping.getFactory(new Signature(Object.class, Object.class), new Signature(boolean.class, int.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true) == 42;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load(true);
        verify(storage).save(true, 42);
        verifyNoMoreInteractions(storage);
    }
}
