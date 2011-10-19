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
public class CharacterBooleanCacheBoxValueTest {
    private static final CharacterBooleanCalculatable CALCULATABLE = new CharacterBooleanCalculatable() {
        @Override
        public boolean calculate(Object owner, char o) {
            assert o == '*';
            return true;
        }
    };

    public void testMiss() {
        CharacterObjectStorage storage = mock(CharacterObjectStorage.class);

        when(storage.load('*')).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        CharacterBooleanCache cache = (CharacterBooleanCache) Wrapping.getFactory(new Signature(char.class, Object.class), new Signature(char.class, boolean.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.size() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate('*') == true;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).load('*');
        verify(storage).save('*', true);
        verifyNoMoreInteractions(storage);
    }

    public void testHit() {
        CharacterObjectStorage storage = mock(CharacterObjectStorage.class);

        CharacterBooleanCache cache = (CharacterBooleanCache) Wrapping.getFactory(new Signature(char.class, Object.class), new Signature(char.class, boolean.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        when(storage.load('*')).thenReturn(true);
        when(storage.size()).thenReturn(1);

        assert cache.size() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate('*') == true;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(2)).load('*');
        verifyNoMoreInteractions(storage);
    }

    public void testClear() {
        CharacterObjectStorage storage = mock(CharacterObjectStorage.class);

        CharacterBooleanCache cache = (CharacterBooleanCache) Wrapping.getFactory(new Signature(char.class, Object.class), new Signature(char.class, boolean.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    public void testSetDuringDependencyNodeOperations() {
        CharacterObjectStorage storage = mock(CharacterObjectStorage.class);

        when(storage.load('*')).thenReturn(Storage.UNDEFINED, true);

        CharacterBooleanCalculatable calculatable = mock(CharacterBooleanCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", '*')).thenThrow(new ResourceOccupied(r));

        CharacterBooleanCache cache = (CharacterBooleanCache) Wrapping.getFactory(new Signature(char.class, Object.class), new Signature(char.class, boolean.class), false).
                wrap("123", calculatable, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate('*') == true;

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

        CharacterBooleanCache cache = (CharacterBooleanCache) Wrapping.getFactory(new Signature(char.class, Object.class), new Signature(char.class, boolean.class), false).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate('*') == true;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load('*');
        verify(storage).save('*', true);
        verifyNoMoreInteractions(storage);
    }
}
