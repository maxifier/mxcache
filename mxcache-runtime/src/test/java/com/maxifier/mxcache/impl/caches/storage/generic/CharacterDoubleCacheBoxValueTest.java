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
public class CharacterDoubleCacheBoxValueTest {
    private static final CharacterDoubleCalculatable CALCULATABLE = new CharacterDoubleCalculatable() {
        @Override
        public double calculate(Object owner, char o) {
            assert o == '*';
            return 42d;
        }
    };

    public void testMiss() {
        CharacterObjectStorage storage = mock(CharacterObjectStorage.class);

        when(storage.load('*')).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        CharacterDoubleCache cache = (CharacterDoubleCache) Wrapping.getFactory(new Signature(char.class, Object.class), new Signature(char.class, double.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate('*') == 42d;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).load('*');
        verify(storage).save('*', 42d);
        verifyNoMoreInteractions(storage);
    }

    public void testHit() {
        CharacterObjectStorage storage = mock(CharacterObjectStorage.class);

        CharacterDoubleCache cache = (CharacterDoubleCache) Wrapping.getFactory(new Signature(char.class, Object.class), new Signature(char.class, double.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(storage.load('*')).thenReturn(42d);
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate('*') == 42d;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(2)).load('*');
        verifyNoMoreInteractions(storage);
    }

    public void testClear() {
        CharacterObjectStorage storage = mock(CharacterObjectStorage.class);

        CharacterDoubleCache cache = (CharacterDoubleCache) Wrapping.getFactory(new Signature(char.class, Object.class), new Signature(char.class, double.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        CharacterObjectStorage storage = mock(CharacterObjectStorage.class);

        when(storage.load('*')).thenReturn(Storage.UNDEFINED, 42d);

        CharacterDoubleCalculatable calculatable = mock(CharacterDoubleCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", '*')).thenThrow(new ResourceOccupied(r));

        CharacterDoubleCache cache = (CharacterDoubleCache) Wrapping.getFactory(new Signature(char.class, Object.class), new Signature(char.class, double.class), false).
                wrap("123", calculatable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate('*') == 42d;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(2)).load('*');
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", '*');
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        CharacterObjectStorage storage = mock(CharacterObjectStorage.class);

        when(storage.load('*')).thenReturn(Storage.UNDEFINED);

        CharacterDoubleCache cache = (CharacterDoubleCache) Wrapping.getFactory(new Signature(char.class, Object.class), new Signature(char.class, double.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate('*') == 42d;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load('*');
        verify(storage).save('*', 42d);
        verifyNoMoreInteractions(storage);
    }
}
