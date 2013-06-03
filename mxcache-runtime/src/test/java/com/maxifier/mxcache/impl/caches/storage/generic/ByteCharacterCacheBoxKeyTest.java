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
public class ByteCharacterCacheBoxKeyTest {
    private static final ByteCharacterCalculatable CALCULATABLE = new ByteCharacterCalculatable() {
        @Override
        public char calculate(Object owner, byte o) {
            assert o == (byte)42;
            return '*';
        }
    };

    public void testMiss() {
        ObjectCharacterStorage storage = mock(ObjectCharacterStorage.class);

        when(storage.isCalculated((byte)42)).thenReturn(false);
        when(storage.size()).thenReturn(0);

        ByteCharacterCache cache = (ByteCharacterCache) Wrapping.getFactory(new Signature(Object.class, char.class), new Signature(byte.class, char.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42) == '*';

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated((byte)42);
        verify(storage).save((byte)42, '*');
        verifyNoMoreInteractions(storage);
    }

    public void testHit() {
        ObjectCharacterStorage storage = mock(ObjectCharacterStorage.class);

        ByteCharacterCache cache = (ByteCharacterCache) Wrapping.getFactory(new Signature(Object.class, char.class), new Signature(byte.class, char.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(storage.isCalculated((byte)42)).thenReturn(true);
        when(storage.load((byte)42)).thenReturn('*');
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42) == '*';

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated((byte)42);
        verify(storage).load((byte)42);
        verifyNoMoreInteractions(storage);
    }

    public void testClear() {
        ObjectCharacterStorage storage = mock(ObjectCharacterStorage.class);

        ByteCharacterCache cache = (ByteCharacterCache) Wrapping.getFactory(new Signature(Object.class, char.class), new Signature(byte.class, char.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        ObjectCharacterStorage storage = mock(ObjectCharacterStorage.class);

        when(storage.isCalculated((byte)42)).thenReturn(false, true);
        when(storage.load((byte)42)).thenReturn('*');

        ByteCharacterCalculatable calculatable = mock(ByteCharacterCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", (byte)42)).thenThrow(new ResourceOccupied(r));

        ByteCharacterCache cache = (ByteCharacterCache) Wrapping.getFactory(new Signature(Object.class, char.class), new Signature(byte.class, char.class), false).
                wrap("123", calculatable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42) == '*';

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).isCalculated((byte)42);
        verify(storage).load((byte)42);
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", (byte)42);
        verifyNoMoreInteractions(calculatable);
    }

    public void testResetStat() {
        ObjectCharacterStorage storage = mock(ObjectCharacterStorage.class);

        when(storage.isCalculated((byte)42)).thenReturn(false);

        ByteCharacterCache cache = (ByteCharacterCache) Wrapping.getFactory(new Signature(Object.class, char.class), new Signature(byte.class, char.class), false).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42) == '*';

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).isCalculated((byte)42);
        verify(storage).save((byte)42, '*');
        verifyNoMoreInteractions(storage);
    }
}
