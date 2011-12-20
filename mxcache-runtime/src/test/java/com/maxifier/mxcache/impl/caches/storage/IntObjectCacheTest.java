package com.maxifier.mxcache.impl.caches.storage;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.storage.IntObjectStorage;
import com.maxifier.mxcache.storage.Storage;
import com.maxifier.mxcache.storage.elementlocked.IntObjectElementLockedStorage;
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
public class IntObjectCacheTest {
    private static final Signature SINGATURE = new Signature(int.class, Object.class);

    private static final IntObjectCalculatable CALCULATABLE = new IntObjectCalculatable() {
        @Override
        public Object calculate(Object owner, int o) {
            assert o == 42;
            return "123";
        }
    };

    @DataProvider(name = "both")
    public Object[][] v200v210v219() {
        return new Object[][] {{false}, {true}};
    }

    @Test(dataProvider = "both")
    public void testMiss(boolean elementLocked) {
        IntObjectStorage storage = createStorage(elementLocked);

        when(storage.load(42)).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        IntObjectCache cache = (IntObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42).equals("123");

        verify(storage).size();
        verify(storage, atLeast(1)).load(42);
        verify(storage).save(42, "123");
        if (elementLocked) {
            
                ((IntObjectElementLockedStorage)verify(storage, atLeast(1))).lock(42);
                ((IntObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(42);
            
        }
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;
    }

    @Test(dataProvider = "both")
    public void testHit(boolean elementLocked) {
        IntObjectStorage storage = createStorage(elementLocked);

        IntObjectCache cache = (IntObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(storage.load(42)).thenReturn("123");
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42).equals("123");

        verify(storage).size();
        verify(storage, atLeast(1)).load(42);
        if (elementLocked) {
            
                ((IntObjectElementLockedStorage)verify(storage, atLeast(1))).lock(42);
                ((IntObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(42);
            
        }
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;
    }

    @Test(dataProvider = "both")
    public void testClear(boolean elementLocked) {
        IntObjectStorage storage = createStorage(elementLocked);

        IntObjectCache cache = (IntObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testSetDuringDependencyNodeOperations(boolean elementLocked) {
        IntObjectStorage storage = createStorage(elementLocked);

        when(storage.load(42)).thenReturn(Storage.UNDEFINED, "123");

        IntObjectCalculatable calculatable = mock(IntObjectCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", 42)).thenThrow(new ResourceOccupied(r));

        IntObjectCache cache = (IntObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", calculatable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42).equals("123");

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).load(42);
        if (elementLocked) {
            
                ((IntObjectElementLockedStorage)verify(storage, atLeast(1))).lock(42);
                ((IntObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(42);
            
        }
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", 42);
        verifyNoMoreInteractions(calculatable);
    }

    @Test(dataProvider = "both")
    public void testResetStat(boolean elementLocked) {
        IntObjectStorage storage = createStorage(elementLocked);

        when(storage.load(42)).thenReturn(Storage.UNDEFINED);

        IntObjectCache cache = (IntObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42).equals("123");

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load(42);
        verify(storage).save(42, "123");
        if (elementLocked) {
            
                ((IntObjectElementLockedStorage)verify(storage, atLeast(1))).lock(42);
                ((IntObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(42);
               
        }
        verifyNoMoreInteractions(storage);
    }

    private IntObjectStorage createStorage(boolean elementLocked) {
        IntObjectStorage storage = mock(elementLocked ? IntObjectElementLockedStorage.class : IntObjectStorage.class);
        if (elementLocked) {
            when(((IntObjectElementLockedStorage)storage).getLock()).thenReturn(new ReentrantLock());
        }
        return storage;
    }

    @Test(dataProvider = "both")
    public void testTransparentStat(boolean elementLocked) {
        IntObjectStorage storage = mock(elementLocked ? IntObjectElementLockedStorage.class : IntObjectStorage.class, withSettings().extraInterfaces(StatisticsHolder.class));

        IntObjectCache cache = (IntObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.getStatistics();

        verify((StatisticsHolder)storage).getStatistics();
        verifyNoMoreInteractions(storage);
    }
}
