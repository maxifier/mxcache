/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.storage;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.impl.wrapping.Wrapping;
import com.maxifier.mxcache.storage.CharacterObjectStorage;
import com.maxifier.mxcache.storage.Storage;
import com.maxifier.mxcache.storage.elementlocked.CharacterObjectElementLockedStorage;
import com.maxifier.mxcache.provider.Signature;
import com.maxifier.mxcache.resource.MxResource;
import com.maxifier.mxcache.impl.MutableStatisticsImpl;
import com.maxifier.mxcache.impl.resource.DependencyTracker;
import com.maxifier.mxcache.impl.resource.ResourceOccupied;
import com.maxifier.mxcache.interfaces.StatisticsHolder;
import org.testng.annotations.*;

import java.util.concurrent.locks.*;

import static org.mockito.Mockito.*;

/**
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2OCacheTest.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class CharacterObjectCacheTest {
    private static final Signature SINGATURE = new Signature(char.class, Object.class);

    private static final CharacterObjectCalculatable CALCULATABLE = new CharacterObjectCalculatable() {
        @Override
        public Object calculate(Object owner, char o) {
            assert o == '*';
            return "123";
        }
    };

    @DataProvider(name = "both")
    public Object[][] v200v210v219() {
        return new Object[][] {{false}, {true}};
    }

    @Test(dataProvider = "both")
    public void testMiss(boolean elementLocked) {
        CharacterObjectStorage storage = createStorage(elementLocked);

        when(storage.load('*')).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        CharacterObjectCache cache = (CharacterObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate('*').equals("123");

        verify(storage).size();
        verify(storage, atLeast(1)).load('*');
        verify(storage).save('*', "123");
        if (elementLocked) {
            
                ((CharacterObjectElementLockedStorage)verify(storage, atLeast(1))).lock('*');
                ((CharacterObjectElementLockedStorage)verify(storage, atLeast(1))).unlock('*');
            
        }
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;
    }

    @Test(dataProvider = "both")
    public void testHit(boolean elementLocked) {
        CharacterObjectStorage storage = createStorage(elementLocked);

        CharacterObjectCache cache = (CharacterObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(storage.load('*')).thenReturn("123");
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate('*').equals("123");

        verify(storage).size();
        verify(storage, atLeast(1)).load('*');
        if (elementLocked) {
            
                ((CharacterObjectElementLockedStorage)verify(storage, atLeast(1))).lock('*');
                ((CharacterObjectElementLockedStorage)verify(storage, atLeast(1))).unlock('*');
            
        }
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;
    }

    @Test(dataProvider = "both")
    public void testClear(boolean elementLocked) {
        CharacterObjectStorage storage = createStorage(elementLocked);

        CharacterObjectCache cache = (CharacterObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testSetDuringDependencyNodeOperations(boolean elementLocked) {
        CharacterObjectStorage storage = createStorage(elementLocked);

        when(storage.load('*')).thenReturn(Storage.UNDEFINED, "123");

        CharacterObjectCalculatable calculatable = mock(CharacterObjectCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", '*')).thenThrow(new ResourceOccupied(r));

        CharacterObjectCache cache = (CharacterObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", calculatable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate('*').equals("123");

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).load('*');
        if (elementLocked) {
            
                ((CharacterObjectElementLockedStorage)verify(storage, atLeast(1))).lock('*');
                ((CharacterObjectElementLockedStorage)verify(storage, atLeast(1))).unlock('*');
            
        }
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", '*');
        verifyNoMoreInteractions(calculatable);
    }

    @Test(dataProvider = "both")
    public void testResetStat(boolean elementLocked) {
        CharacterObjectStorage storage = createStorage(elementLocked);

        when(storage.load('*')).thenReturn(Storage.UNDEFINED);

        CharacterObjectCache cache = (CharacterObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate('*').equals("123");

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load('*');
        verify(storage).save('*', "123");
        if (elementLocked) {
            
                ((CharacterObjectElementLockedStorage)verify(storage, atLeast(1))).lock('*');
                ((CharacterObjectElementLockedStorage)verify(storage, atLeast(1))).unlock('*');
               
        }
        verifyNoMoreInteractions(storage);
    }

    private CharacterObjectStorage createStorage(boolean elementLocked) {
        // cast necessary for JDK8 compilation
        CharacterObjectStorage storage = mock((Class<CharacterObjectStorage>)(elementLocked ? CharacterObjectElementLockedStorage.class : CharacterObjectStorage.class));
        if (elementLocked) {
            when(((CharacterObjectElementLockedStorage)storage).getLock()).thenReturn(new ReentrantLock());
        }
        return storage;
    }

    @Test(dataProvider = "both")
    public void testTransparentStat(boolean elementLocked) {
        // cast necessary for JDK8 compilation
        CharacterObjectStorage storage = mock((Class<CharacterObjectStorage>)(elementLocked ? CharacterObjectElementLockedStorage.class : CharacterObjectStorage.class), withSettings().extraInterfaces(StatisticsHolder.class));

        CharacterObjectCache cache = (CharacterObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.getStatistics();

        verify((StatisticsHolder)storage).getStatistics();
        verifyNoMoreInteractions(storage);
    }
}
