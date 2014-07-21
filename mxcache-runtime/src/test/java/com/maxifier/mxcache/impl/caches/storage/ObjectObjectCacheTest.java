/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.storage;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.impl.wrapping.Wrapping;
import com.maxifier.mxcache.storage.ObjectObjectStorage;
import com.maxifier.mxcache.storage.Storage;
import com.maxifier.mxcache.storage.elementlocked.ObjectObjectElementLockedStorage;
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
public class ObjectObjectCacheTest {
    private static final Signature SINGATURE = new Signature(Object.class, Object.class);

    private static final ObjectObjectCalculatable CALCULATABLE = new ObjectObjectCalculatable() {
        @Override
        public Object calculate(Object owner, Object o) {
            assert o == "123";
            return "123";
        }
    };

    @DataProvider(name = "both")
    public Object[][] v200v210v219() {
        return new Object[][] {{false}, {true}};
    }

    @Test(dataProvider = "both")
    public void testMiss(boolean elementLocked) {
        ObjectObjectStorage storage = createStorage(elementLocked);

        when(storage.load("123")).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        ObjectObjectCache cache = (ObjectObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate("123").equals("123");

        verify(storage).size();
        verify(storage, atLeast(1)).load("123");
        verify(storage).save("123", "123");
        if (elementLocked) {
            
                ((ObjectObjectElementLockedStorage)verify(storage, atLeast(1))).lock("123");
                ((ObjectObjectElementLockedStorage)verify(storage, atLeast(1))).unlock("123");
            
        }
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;
    }

    @Test(dataProvider = "both")
    public void testHit(boolean elementLocked) {
        ObjectObjectStorage storage = createStorage(elementLocked);

        ObjectObjectCache cache = (ObjectObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(storage.load("123")).thenReturn("123");
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate("123").equals("123");

        verify(storage).size();
        verify(storage, atLeast(1)).load("123");
        if (elementLocked) {
            
                ((ObjectObjectElementLockedStorage)verify(storage, atLeast(1))).lock("123");
                ((ObjectObjectElementLockedStorage)verify(storage, atLeast(1))).unlock("123");
            
        }
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;
    }

    @Test(dataProvider = "both")
    public void testClear(boolean elementLocked) {
        ObjectObjectStorage storage = createStorage(elementLocked);

        ObjectObjectCache cache = (ObjectObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testSetDuringDependencyNodeOperations(boolean elementLocked) {
        ObjectObjectStorage storage = createStorage(elementLocked);

        when(storage.load("123")).thenReturn(Storage.UNDEFINED, "123");

        ObjectObjectCalculatable calculatable = mock(ObjectObjectCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", "123")).thenThrow(new ResourceOccupied(r));

        ObjectObjectCache cache = (ObjectObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", calculatable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate("123").equals("123");

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).load("123");
        if (elementLocked) {
            
                ((ObjectObjectElementLockedStorage)verify(storage, atLeast(1))).lock("123");
                ((ObjectObjectElementLockedStorage)verify(storage, atLeast(1))).unlock("123");
            
        }
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", "123");
        verifyNoMoreInteractions(calculatable);
    }

    @Test(dataProvider = "both")
    public void testResetStat(boolean elementLocked) {
        ObjectObjectStorage storage = createStorage(elementLocked);

        when(storage.load("123")).thenReturn(Storage.UNDEFINED);

        ObjectObjectCache cache = (ObjectObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate("123").equals("123");

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load("123");
        verify(storage).save("123", "123");
        if (elementLocked) {
            
                ((ObjectObjectElementLockedStorage)verify(storage, atLeast(1))).lock("123");
                ((ObjectObjectElementLockedStorage)verify(storage, atLeast(1))).unlock("123");
               
        }
        verifyNoMoreInteractions(storage);
    }

    private ObjectObjectStorage createStorage(boolean elementLocked) {
        // cast necessary for JDK8 compilation
        ObjectObjectStorage storage = mock((Class<ObjectObjectStorage>)(elementLocked ? ObjectObjectElementLockedStorage.class : ObjectObjectStorage.class));
        if (elementLocked) {
            when(((ObjectObjectElementLockedStorage)storage).getLock()).thenReturn(new ReentrantLock());
        }
        return storage;
    }

    @Test(dataProvider = "both")
    public void testTransparentStat(boolean elementLocked) {
        // cast necessary for JDK8 compilation
        ObjectObjectStorage storage = mock((Class<ObjectObjectStorage>)(elementLocked ? ObjectObjectElementLockedStorage.class : ObjectObjectStorage.class), withSettings().extraInterfaces(StatisticsHolder.class));

        ObjectObjectCache cache = (ObjectObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.getStatistics();

        verify((StatisticsHolder)storage).getStatistics();
        verifyNoMoreInteractions(storage);
    }
}
