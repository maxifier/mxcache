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
public class ShortCharacterCacheBoxValueTest {
    private static final ShortCharacterCalculatable CALCULATABLE = new ShortCharacterCalculatable() {
        @Override
        public char calculate(Object owner, short o) {
            assert o == (short)42;
            return '*';
        }
    };

    public void testMiss() {
        ShortObjectStorage storage = mock(ShortObjectStorage.class);

        when(storage.load((short)42)).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        ShortCharacterCache cache = (ShortCharacterCache) Wrapping.getFactory(new Signature(short.class, Object.class), new Signature(short.class, char.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((short)42) == '*';

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).load((short)42);
        verify(storage).save((short)42, '*');
        verifyNoMoreInteractions(storage);
    }

    public void testHit() {
        ShortObjectStorage storage = mock(ShortObjectStorage.class);

        ShortCharacterCache cache = (ShortCharacterCache) Wrapping.getFactory(new Signature(short.class, Object.class), new Signature(short.class, char.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(storage.load((short)42)).thenReturn('*');
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((short)42) == '*';

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(2)).load((short)42);
        verifyNoMoreInteractions(storage);
    }

    public void testClear() {
        ShortObjectStorage storage = mock(ShortObjectStorage.class);

        ShortCharacterCache cache = (ShortCharacterCache) Wrapping.getFactory(new Signature(short.class, Object.class), new Signature(short.class, char.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        ShortObjectStorage storage = mock(ShortObjectStorage.class);

        when(storage.load((short)42)).thenReturn(Storage.UNDEFINED, '*');

        ShortCharacterCalculatable calculatable = mock(ShortCharacterCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", (short)42)).thenThrow(new ResourceOccupied(r));

        ShortCharacterCache cache = (ShortCharacterCache) Wrapping.getFactory(new Signature(short.class, Object.class), new Signature(short.class, char.class), false).
                wrap("123", calculatable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((short)42) == '*';

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(2)).load((short)42);
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", (short)42);
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        ShortObjectStorage storage = mock(ShortObjectStorage.class);

        when(storage.load((short)42)).thenReturn(Storage.UNDEFINED);

        ShortCharacterCache cache = (ShortCharacterCache) Wrapping.getFactory(new Signature(short.class, Object.class), new Signature(short.class, char.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((short)42) == '*';

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load((short)42);
        verify(storage).save((short)42, '*');
        verifyNoMoreInteractions(storage);
    }
}
