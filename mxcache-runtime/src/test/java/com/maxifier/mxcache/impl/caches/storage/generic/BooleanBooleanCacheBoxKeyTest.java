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
public class BooleanBooleanCacheBoxKeyTest {
    private static final BooleanBooleanCalculatable CALCULATABLE = new BooleanBooleanCalculatable() {
        @Override
        public boolean calculate(Object owner, boolean o) {
            assert o == true;
            return true;
        }
    };

    public void testMiss() {
        ObjectBooleanStorage storage = mock(ObjectBooleanStorage.class);

        when(storage.isCalculated(true)).thenReturn(false);
        when(storage.size()).thenReturn(0);

        BooleanBooleanCache cache = (BooleanBooleanCache) Wrapping.getFactory(new Signature(Object.class, boolean.class), new Signature(boolean.class, boolean.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true) == true;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated(true);
        verify(storage).save(true, true);
        verifyNoMoreInteractions(storage);
    }

    public void testHit() {
        ObjectBooleanStorage storage = mock(ObjectBooleanStorage.class);

        BooleanBooleanCache cache = (BooleanBooleanCache) Wrapping.getFactory(new Signature(Object.class, boolean.class), new Signature(boolean.class, boolean.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(storage.isCalculated(true)).thenReturn(true);
        when(storage.load(true)).thenReturn(true);
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true) == true;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated(true);
        verify(storage).load(true);
        verifyNoMoreInteractions(storage);
    }

    public void testClear() {
        ObjectBooleanStorage storage = mock(ObjectBooleanStorage.class);

        BooleanBooleanCache cache = (BooleanBooleanCache) Wrapping.getFactory(new Signature(Object.class, boolean.class), new Signature(boolean.class, boolean.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        ObjectBooleanStorage storage = mock(ObjectBooleanStorage.class);

        when(storage.isCalculated(true)).thenReturn(false, true);
        when(storage.load(true)).thenReturn(true);

        BooleanBooleanCalculatable calculatable = mock(BooleanBooleanCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", true)).thenThrow(new ResourceOccupied(r));

        BooleanBooleanCache cache = (BooleanBooleanCache) Wrapping.getFactory(new Signature(Object.class, boolean.class), new Signature(boolean.class, boolean.class), false).
                wrap("123", calculatable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true) == true;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).isCalculated(true);
        verify(storage).load(true);
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", true);
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        ObjectBooleanStorage storage = mock(ObjectBooleanStorage.class);

        when(storage.isCalculated(true)).thenReturn(false);

        BooleanBooleanCache cache = (BooleanBooleanCache) Wrapping.getFactory(new Signature(Object.class, boolean.class), new Signature(boolean.class, boolean.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true) == true;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).isCalculated(true);
        verify(storage).save(true, true);
        verifyNoMoreInteractions(storage);
    }
}
