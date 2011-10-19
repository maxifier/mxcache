package com.maxifier.mxcache.impl.caches.storage;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.storage.BooleanObjectStorage;
import com.maxifier.mxcache.storage.Storage;
import com.maxifier.mxcache.storage.elementlocked.BooleanObjectElementLockedStorage;
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
public class BooleanObjectCacheTest {
    private static final Signature SINGATURE = new Signature(boolean.class, Object.class);

    private static final BooleanObjectCalculatable CALCULATABLE = new BooleanObjectCalculatable() {
        @Override
        public Object calculate(Object owner, boolean o) {
            assert o == true;
            return "123";
        }
    };

    @DataProvider(name = "both")
    public Object[][] v200v210v219() {
        return new Object[][] {{false}, {true}};
    }

    @Test(dataProvider = "both")
    public void testMiss(boolean elementLocked) {
        BooleanObjectStorage storage = createStorage(elementLocked);

        when(storage.load(true)).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        BooleanObjectCache cache = (BooleanObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.size() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true).equals("123");

        verify(storage).size();
        verify(storage, atLeast(1)).load(true);
        verify(storage).save(true, "123");
        if (elementLocked) {
            
                ((BooleanObjectElementLockedStorage)verify(storage, atLeast(1))).lock(true);
                ((BooleanObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(true);
            
        }
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;
    }

    @Test(dataProvider = "both")
    public void testHit(boolean elementLocked) {
        BooleanObjectStorage storage = createStorage(elementLocked);

        BooleanObjectCache cache = (BooleanObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        when(storage.load(true)).thenReturn("123");
        when(storage.size()).thenReturn(1);

        assert cache.size() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true).equals("123");

        verify(storage).size();
        verify(storage, atLeast(1)).load(true);
        if (elementLocked) {
            
                ((BooleanObjectElementLockedStorage)verify(storage, atLeast(1))).lock(true);
                ((BooleanObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(true);
            
        }
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;
    }

    @Test(dataProvider = "both")
    public void testClear(boolean elementLocked) {
        BooleanObjectStorage storage = createStorage(elementLocked);

        BooleanObjectCache cache = (BooleanObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testSetDuringDependencyNodeOperations(boolean elementLocked) {
        BooleanObjectStorage storage = createStorage(elementLocked);

        when(storage.load(true)).thenReturn(Storage.UNDEFINED, "123");

        BooleanObjectCalculatable calculatable = mock(BooleanObjectCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", true)).thenThrow(new ResourceOccupied(r));

        BooleanObjectCache cache = (BooleanObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", calculatable, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true).equals("123");

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).load(true);
        if (elementLocked) {
            
                ((BooleanObjectElementLockedStorage)verify(storage, atLeast(1))).lock(true);
                ((BooleanObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(true);
            
        }
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", true);
        verifyNoMoreInteractions(calculatable);
    }

    @Test(dataProvider = "both")
    public void testResetStat(boolean elementLocked) {
        BooleanObjectStorage storage = createStorage(elementLocked);

        when(storage.load(true)).thenReturn(Storage.UNDEFINED);

        BooleanObjectCache cache = (BooleanObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true).equals("123");

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load(true);
        verify(storage).save(true, "123");
        if (elementLocked) {
            
                ((BooleanObjectElementLockedStorage)verify(storage, atLeast(1))).lock(true);
                ((BooleanObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(true);
               
        }
        verifyNoMoreInteractions(storage);
    }

    private BooleanObjectStorage createStorage(boolean elementLocked) {
        BooleanObjectStorage storage = mock(elementLocked ? BooleanObjectElementLockedStorage.class : BooleanObjectStorage.class);
        if (elementLocked) {
            when(((BooleanObjectElementLockedStorage)storage).getLock()).thenReturn(new ReentrantLock());
        }
        return storage;
    }

    @Test(dataProvider = "both")
    public void testTransparentStat(boolean elementLocked) {
        BooleanObjectStorage storage = mock(elementLocked ? BooleanObjectElementLockedStorage.class : BooleanObjectStorage.class, withSettings().extraInterfaces(StatisticsHolder.class));

        BooleanObjectCache cache = (BooleanObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        cache.getStatistics();

        verify((StatisticsHolder)storage).getStatistics();
        verifyNoMoreInteractions(storage);
    }
}
