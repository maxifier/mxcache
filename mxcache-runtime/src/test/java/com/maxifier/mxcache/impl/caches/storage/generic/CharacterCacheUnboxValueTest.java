package com.maxifier.mxcache.impl.caches.storage.generic;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.storage.*;
import org.testng.annotations.Test;
import com.maxifier.mxcache.resource.MxResource;
import com.maxifier.mxcache.impl.MutableStatisticsImpl;
import com.maxifier.mxcache.impl.resource.DependencyTracker;
import com.maxifier.mxcache.impl.resource.ResourceOccupied;
import static org.testng.Assert.*;

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
public class CharacterCacheUnboxValueTest {
    private static final ObjectCalculatable CALCULATABLE = new ObjectCalculatable() {
        @Override
        public Character calculate(Object owner) {
            return '*';
        }
    };

    public void testMiss() {
        CharacterStorage storage = mock(CharacterStorage.class);

        when(storage.isCalculated()).thenReturn(false);
        when(storage.size()).thenReturn(0);

        ObjectCache cache = (ObjectCache) Wrapping.getFactory(new Signature(null, char.class), new Signature(null, Object.class), false).
                    wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assertEquals(cache.getOrCreate(), '*');

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated();
        verify(storage).save('*');
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;
    }

    public void testHit() {
        CharacterStorage storage = mock(CharacterStorage.class);

        ObjectCache cache = (ObjectCache) Wrapping.getFactory(new Signature(null, char.class), new Signature(null, Object.class), false).
                    wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        when(storage.isCalculated()).thenReturn(true);
        when(storage.load()).thenReturn('*');
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assertEquals(cache.getOrCreate(), '*');

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated();
        verify(storage).load();
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;
    }

    public void testClear() {
        CharacterStorage storage = mock(CharacterStorage.class);

        ObjectCache cache = (ObjectCache) Wrapping.getFactory(new Signature(null, char.class), new Signature(null, Object.class), false).
                    wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        CharacterStorage storage = mock(CharacterStorage.class);

        when(storage.isCalculated()).thenReturn(false, true);
        when(storage.load()).thenReturn('*');

        ObjectCalculatable calculatable = mock(ObjectCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123")).thenThrow(new ResourceOccupied(r));

        ObjectCache cache = (ObjectCache) Wrapping.getFactory(new Signature(null, char.class), new Signature(null, Object.class), false).
                    wrap("123", calculatable, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assertEquals(cache.getOrCreate(), '*');

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).isCalculated();
        verify(storage).load();
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123");
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        CharacterStorage storage = mock(CharacterStorage.class);

        when(storage.isCalculated()).thenReturn(false);

        ObjectCache cache = (ObjectCache) Wrapping.getFactory(new Signature(null, char.class), new Signature(null, Object.class), false).
                    wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assertEquals(cache.getOrCreate(), '*');

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).isCalculated();
        verify(storage).save('*');
        verifyNoMoreInteractions(storage);
    }
}
