package com.maxifier.mxcache.impl.caches.storage;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.impl.wrapping.Wrapping;
import com.maxifier.mxcache.storage.DoubleObjectStorage;
import com.maxifier.mxcache.storage.Storage;
import com.maxifier.mxcache.storage.elementlocked.DoubleObjectElementLockedStorage;
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
public class DoubleObjectCacheTest {
    private static final Signature SINGATURE = new Signature(double.class, Object.class);

    private static final DoubleObjectCalculatable CALCULATABLE = new DoubleObjectCalculatable() {
        @Override
        public Object calculate(Object owner, double o) {
            assert o == 42d;
            return "123";
        }
    };

    @DataProvider(name = "both")
    public Object[][] v200v210v219() {
        return new Object[][] {{false}, {true}};
    }

    @Test(dataProvider = "both")
    public void testMiss(boolean elementLocked) {
        DoubleObjectStorage storage = createStorage(elementLocked);

        when(storage.load(42d)).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        DoubleObjectCache cache = (DoubleObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42d).equals("123");

        verify(storage).size();
        verify(storage, atLeast(1)).load(42d);
        verify(storage).save(42d, "123");
        if (elementLocked) {
            
                ((DoubleObjectElementLockedStorage)verify(storage, atLeast(1))).lock(42d);
                ((DoubleObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(42d);
            
        }
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;
    }

    @Test(dataProvider = "both")
    public void testHit(boolean elementLocked) {
        DoubleObjectStorage storage = createStorage(elementLocked);

        DoubleObjectCache cache = (DoubleObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(storage.load(42d)).thenReturn("123");
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42d).equals("123");

        verify(storage).size();
        verify(storage, atLeast(1)).load(42d);
        if (elementLocked) {
            
                ((DoubleObjectElementLockedStorage)verify(storage, atLeast(1))).lock(42d);
                ((DoubleObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(42d);
            
        }
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;
    }

    @Test(dataProvider = "both")
    public void testClear(boolean elementLocked) {
        DoubleObjectStorage storage = createStorage(elementLocked);

        DoubleObjectCache cache = (DoubleObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testSetDuringDependencyNodeOperations(boolean elementLocked) {
        DoubleObjectStorage storage = createStorage(elementLocked);

        when(storage.load(42d)).thenReturn(Storage.UNDEFINED, "123");

        DoubleObjectCalculatable calculatable = mock(DoubleObjectCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", 42d)).thenThrow(new ResourceOccupied(r));

        DoubleObjectCache cache = (DoubleObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", calculatable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42d).equals("123");

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).load(42d);
        if (elementLocked) {
            
                ((DoubleObjectElementLockedStorage)verify(storage, atLeast(1))).lock(42d);
                ((DoubleObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(42d);
            
        }
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", 42d);
        verifyNoMoreInteractions(calculatable);
    }

    @Test(dataProvider = "both")
    public void testResetStat(boolean elementLocked) {
        DoubleObjectStorage storage = createStorage(elementLocked);

        when(storage.load(42d)).thenReturn(Storage.UNDEFINED);

        DoubleObjectCache cache = (DoubleObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42d).equals("123");

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load(42d);
        verify(storage).save(42d, "123");
        if (elementLocked) {
            
                ((DoubleObjectElementLockedStorage)verify(storage, atLeast(1))).lock(42d);
                ((DoubleObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(42d);
               
        }
        verifyNoMoreInteractions(storage);
    }

    private DoubleObjectStorage createStorage(boolean elementLocked) {
        DoubleObjectStorage storage = mock(elementLocked ? DoubleObjectElementLockedStorage.class : DoubleObjectStorage.class);
        if (elementLocked) {
            when(((DoubleObjectElementLockedStorage)storage).getLock()).thenReturn(new ReentrantLock());
        }
        return storage;
    }

    @Test(dataProvider = "both")
    public void testTransparentStat(boolean elementLocked) {
        DoubleObjectStorage storage = mock(elementLocked ? DoubleObjectElementLockedStorage.class : DoubleObjectStorage.class, withSettings().extraInterfaces(StatisticsHolder.class));

        DoubleObjectCache cache = (DoubleObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.getStatistics();

        verify((StatisticsHolder)storage).getStatistics();
        verifyNoMoreInteractions(storage);
    }
}
