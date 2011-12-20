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
public class FloatCacheBoxValueTest {
    private static final FloatCalculatable CALCULATABLE = new FloatCalculatable() {
        @Override
        public float calculate(Object owner) {
            return 42f;
        }
    };

    public void testMiss() {
        ObjectStorage storage = mock(ObjectStorage.class);

        when(storage.load()).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        FloatCache cache = (FloatCache) Wrapping.getFactory(new Signature(null, Object.class), new Signature(null, float.class), false).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate() == 42f;

        verify(storage).size();
        verify(storage, atLeast(1)).load();
        verify(storage).save(42f);
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;
    }

    public void testHit() {
        ObjectStorage storage = mock(ObjectStorage.class);

        FloatCache cache = (FloatCache) Wrapping.getFactory(new Signature(null, Object.class), new Signature(null, float.class), false).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(storage.load()).thenReturn(42f);
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate() == 42f;

        verify(storage).size();
        verify(storage, atLeast(1)).load();
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;
    }

    public void testClear() {
        ObjectStorage storage = mock(ObjectStorage.class);

        FloatCache cache = (FloatCache) Wrapping.getFactory(new Signature(null, Object.class), new Signature(null, float.class), false).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        ObjectStorage storage = mock(ObjectStorage.class);

        when(storage.load()).thenReturn(Storage.UNDEFINED, 42f);

        FloatCalculatable calculatable = mock(FloatCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123")).thenThrow(new ResourceOccupied(r));

        FloatCache cache = (FloatCache) Wrapping.getFactory(new Signature(null, Object.class), new Signature(null, float.class), false).
                    wrap("123", calculatable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate() == 42f;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(2)).load();
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123");
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        ObjectStorage storage = mock(ObjectStorage.class);

        when(storage.load()).thenReturn(Storage.UNDEFINED);

        FloatCache cache = (FloatCache) Wrapping.getFactory(new Signature(null, Object.class), new Signature(null, float.class), false).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate() == 42f;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load();
        verify(storage).save(42f);
        verifyNoMoreInteractions(storage);
    }
}
