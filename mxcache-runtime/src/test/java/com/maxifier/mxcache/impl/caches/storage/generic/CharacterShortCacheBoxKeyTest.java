/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
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
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PBoxKeyCacheTest.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class CharacterShortCacheBoxKeyTest {
    private static final CharacterShortCalculatable CALCULATABLE = new CharacterShortCalculatable() {
        @Override
        public short calculate(Object owner, char o) {
            assert o == '*';
            return (short)42;
        }
    };

    public void testMiss() {
        ObjectShortStorage storage = mock(ObjectShortStorage.class);

        when(storage.isCalculated('*')).thenReturn(false);
        when(storage.size()).thenReturn(0);

        CharacterShortCache cache = (CharacterShortCache) Wrapping.getFactory(new Signature(Object.class, short.class), new Signature(char.class, short.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate('*') == (short)42;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated('*');
        verify(storage).save('*', (short)42);
        verifyNoMoreInteractions(storage);
    }

    public void testHit() {
        ObjectShortStorage storage = mock(ObjectShortStorage.class);

        CharacterShortCache cache = (CharacterShortCache) Wrapping.getFactory(new Signature(Object.class, short.class), new Signature(char.class, short.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(storage.isCalculated('*')).thenReturn(true);
        when(storage.load('*')).thenReturn((short)42);
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate('*') == (short)42;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated('*');
        verify(storage).load('*');
        verifyNoMoreInteractions(storage);
    }

    public void testClear() {
        ObjectShortStorage storage = mock(ObjectShortStorage.class);

        CharacterShortCache cache = (CharacterShortCache) Wrapping.getFactory(new Signature(Object.class, short.class), new Signature(char.class, short.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        ObjectShortStorage storage = mock(ObjectShortStorage.class);

        when(storage.isCalculated('*')).thenReturn(false, true);
        when(storage.load('*')).thenReturn((short)42);

        CharacterShortCalculatable calculatable = mock(CharacterShortCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", '*')).thenThrow(new ResourceOccupied(r));

        CharacterShortCache cache = (CharacterShortCache) Wrapping.getFactory(new Signature(Object.class, short.class), new Signature(char.class, short.class), false).
                wrap("123", calculatable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate('*') == (short)42;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).isCalculated('*');
        verify(storage).load('*');
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", '*');
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        ObjectShortStorage storage = mock(ObjectShortStorage.class);

        when(storage.isCalculated('*')).thenReturn(false);

        CharacterShortCache cache = (CharacterShortCache) Wrapping.getFactory(new Signature(Object.class, short.class), new Signature(char.class, short.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate('*') == (short)42;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).isCalculated('*');
        verify(storage).save('*', (short)42);
        verifyNoMoreInteractions(storage);
    }
}
