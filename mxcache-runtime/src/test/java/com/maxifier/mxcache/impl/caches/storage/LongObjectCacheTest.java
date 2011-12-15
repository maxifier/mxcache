package com.maxifier.mxcache.impl.caches.storage;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.storage.LongObjectStorage;
import com.maxifier.mxcache.storage.Storage;
import com.maxifier.mxcache.storage.elementlocked.LongObjectElementLockedStorage;
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
public class LongObjectCacheTest {
    private static final Signature SINGATURE = new Signature(long.class, Object.class);

    private static final LongObjectCalculatable CALCULATABLE = new LongObjectCalculatable() {
        @Override
        public Object calculate(Object owner, long o) {
            assert o == 42L;
            return "123";
        }
    };

    @DataProvider(name = "both")
    public Object[][] v200v210v219() {
        return new Object[][] {{false}, {true}};
    }

    @Test(dataProvider = "both")
    public void testMiss(boolean elementLocked) {
        LongObjectStorage storage = createStorage(elementLocked);

        when(storage.load(42L)).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        LongObjectCache cache = (LongObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42L).equals("123");

        verify(storage).size();
        verify(storage, atLeast(1)).load(42L);
        verify(storage).save(42L, "123");
        if (elementLocked) {
            
                ((LongObjectElementLockedStorage)verify(storage, atLeast(1))).lock(42L);
                ((LongObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(42L);
            
        }
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;
    }

    @Test(dataProvider = "both")
    public void testHit(boolean elementLocked) {
        LongObjectStorage storage = createStorage(elementLocked);

        LongObjectCache cache = (LongObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        when(storage.load(42L)).thenReturn("123");
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42L).equals("123");

        verify(storage).size();
        verify(storage, atLeast(1)).load(42L);
        if (elementLocked) {
            
                ((LongObjectElementLockedStorage)verify(storage, atLeast(1))).lock(42L);
                ((LongObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(42L);
            
        }
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;
    }

    @Test(dataProvider = "both")
    public void testClear(boolean elementLocked) {
        LongObjectStorage storage = createStorage(elementLocked);

        LongObjectCache cache = (LongObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testSetDuringDependencyNodeOperations(boolean elementLocked) {
        LongObjectStorage storage = createStorage(elementLocked);

        when(storage.load(42L)).thenReturn(Storage.UNDEFINED, "123");

        LongObjectCalculatable calculatable = mock(LongObjectCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", 42L)).thenThrow(new ResourceOccupied(r));

        LongObjectCache cache = (LongObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", calculatable, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42L).equals("123");

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).load(42L);
        if (elementLocked) {
            
                ((LongObjectElementLockedStorage)verify(storage, atLeast(1))).lock(42L);
                ((LongObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(42L);
            
        }
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", 42L);
        verifyNoMoreInteractions(calculatable);
    }

    @Test(dataProvider = "both")
    public void testResetStat(boolean elementLocked) {
        LongObjectStorage storage = createStorage(elementLocked);

        when(storage.load(42L)).thenReturn(Storage.UNDEFINED);

        LongObjectCache cache = (LongObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42L).equals("123");

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load(42L);
        verify(storage).save(42L, "123");
        if (elementLocked) {
            
                ((LongObjectElementLockedStorage)verify(storage, atLeast(1))).lock(42L);
                ((LongObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(42L);
               
        }
        verifyNoMoreInteractions(storage);
    }

    private LongObjectStorage createStorage(boolean elementLocked) {
        LongObjectStorage storage = mock(elementLocked ? LongObjectElementLockedStorage.class : LongObjectStorage.class);
        if (elementLocked) {
            when(((LongObjectElementLockedStorage)storage).getLock()).thenReturn(new ReentrantLock());
        }
        return storage;
    }

    @Test(dataProvider = "both")
    public void testTransparentStat(boolean elementLocked) {
        LongObjectStorage storage = mock(elementLocked ? LongObjectElementLockedStorage.class : LongObjectStorage.class, withSettings().extraInterfaces(StatisticsHolder.class));

        LongObjectCache cache = (LongObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        cache.getStatistics();

        verify((StatisticsHolder)storage).getStatistics();
        verifyNoMoreInteractions(storage);
    }
}
