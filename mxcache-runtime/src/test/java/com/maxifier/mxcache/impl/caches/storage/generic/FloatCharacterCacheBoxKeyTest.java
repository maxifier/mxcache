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
public class FloatCharacterCacheBoxKeyTest {
    private static final FloatCharacterCalculatable CALCULATABLE = new FloatCharacterCalculatable() {
        @Override
        public char calculate(Object owner, float o) {
            assert o == 42f;
            return '*';
        }
    };

    public void testMiss() {
        ObjectCharacterStorage storage = mock(ObjectCharacterStorage.class);

        when(storage.isCalculated(42f)).thenReturn(false);
        when(storage.size()).thenReturn(0);

        FloatCharacterCache cache = (FloatCharacterCache) Wrapping.getFactory(new Signature(Object.class, char.class), new Signature(float.class, char.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42f) == '*';

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated(42f);
        verify(storage).save(42f, '*');
        verifyNoMoreInteractions(storage);
    }

    public void testHit() {
        ObjectCharacterStorage storage = mock(ObjectCharacterStorage.class);

        FloatCharacterCache cache = (FloatCharacterCache) Wrapping.getFactory(new Signature(Object.class, char.class), new Signature(float.class, char.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        when(storage.isCalculated(42f)).thenReturn(true);
        when(storage.load(42f)).thenReturn('*');
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42f) == '*';

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated(42f);
        verify(storage).load(42f);
        verifyNoMoreInteractions(storage);
    }

    public void testClear() {
        ObjectCharacterStorage storage = mock(ObjectCharacterStorage.class);

        FloatCharacterCache cache = (FloatCharacterCache) Wrapping.getFactory(new Signature(Object.class, char.class), new Signature(float.class, char.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        ObjectCharacterStorage storage = mock(ObjectCharacterStorage.class);

        when(storage.isCalculated(42f)).thenReturn(false, true);
        when(storage.load(42f)).thenReturn('*');

        FloatCharacterCalculatable calculatable = mock(FloatCharacterCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", 42f)).thenThrow(new ResourceOccupied(r));

        FloatCharacterCache cache = (FloatCharacterCache) Wrapping.getFactory(new Signature(Object.class, char.class), new Signature(float.class, char.class), false).
                wrap("123", calculatable, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42f) == '*';

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).isCalculated(42f);
        verify(storage).load(42f);
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", 42f);
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        ObjectCharacterStorage storage = mock(ObjectCharacterStorage.class);

        when(storage.isCalculated(42f)).thenReturn(false);

        FloatCharacterCache cache = (FloatCharacterCache) Wrapping.getFactory(new Signature(Object.class, char.class), new Signature(float.class, char.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42f) == '*';

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).isCalculated(42f);
        verify(storage).save(42f, '*');
        verifyNoMoreInteractions(storage);
    }
}
