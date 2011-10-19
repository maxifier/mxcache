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
public class BooleanCharacterCacheBoxKeyTest {
    private static final BooleanCharacterCalculatable CALCULATABLE = new BooleanCharacterCalculatable() {
        @Override
        public char calculate(Object owner, boolean o) {
            assert o == true;
            return '*';
        }
    };

    public void testMiss() {
        ObjectCharacterStorage storage = mock(ObjectCharacterStorage.class);

        when(storage.isCalculated(true)).thenReturn(false);
        when(storage.size()).thenReturn(0);

        BooleanCharacterCache cache = (BooleanCharacterCache) Wrapping.getFactory(new Signature(Object.class, char.class), new Signature(boolean.class, char.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.size() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true) == '*';

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated(true);
        verify(storage).save(true, '*');
        verifyNoMoreInteractions(storage);
    }

    public void testHit() {
        ObjectCharacterStorage storage = mock(ObjectCharacterStorage.class);

        BooleanCharacterCache cache = (BooleanCharacterCache) Wrapping.getFactory(new Signature(Object.class, char.class), new Signature(boolean.class, char.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        when(storage.isCalculated(true)).thenReturn(true);
        when(storage.load(true)).thenReturn('*');
        when(storage.size()).thenReturn(1);

        assert cache.size() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true) == '*';

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated(true);
        verify(storage).load(true);
        verifyNoMoreInteractions(storage);
    }

    public void testClear() {
        ObjectCharacterStorage storage = mock(ObjectCharacterStorage.class);

        BooleanCharacterCache cache = (BooleanCharacterCache) Wrapping.getFactory(new Signature(Object.class, char.class), new Signature(boolean.class, char.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        ObjectCharacterStorage storage = mock(ObjectCharacterStorage.class);

        when(storage.isCalculated(true)).thenReturn(false, true);
        when(storage.load(true)).thenReturn('*');

        BooleanCharacterCalculatable calculatable = mock(BooleanCharacterCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", true)).thenThrow(new ResourceOccupied(r));

        BooleanCharacterCache cache = (BooleanCharacterCache) Wrapping.getFactory(new Signature(Object.class, char.class), new Signature(boolean.class, char.class), false).
                wrap("123", calculatable, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true) == '*';

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).isCalculated(true);
        verify(storage).load(true);
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", true);
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        ObjectCharacterStorage storage = mock(ObjectCharacterStorage.class);

        when(storage.isCalculated(true)).thenReturn(false);

        BooleanCharacterCache cache = (BooleanCharacterCache) Wrapping.getFactory(new Signature(Object.class, char.class), new Signature(boolean.class, char.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true) == '*';

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).isCalculated(true);
        verify(storage).save(true, '*');
        verifyNoMoreInteractions(storage);
    }
}
