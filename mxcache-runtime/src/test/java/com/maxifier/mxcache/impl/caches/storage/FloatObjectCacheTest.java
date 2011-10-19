package com.maxifier.mxcache.impl.caches.storage;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.storage.FloatObjectStorage;
import com.maxifier.mxcache.storage.Storage;
import com.maxifier.mxcache.storage.elementlocked.FloatObjectElementLockedStorage;
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
public class FloatObjectCacheTest {
    private static final Signature SINGATURE = new Signature(float.class, Object.class);

    private static final FloatObjectCalculatable CALCULATABLE = new FloatObjectCalculatable() {
        @Override
        public Object calculate(Object owner, float o) {
            assert o == 42f;
            return "123";
        }
    };

    @DataProvider(name = "both")
    public Object[][] v200v210v219() {
        return new Object[][] {{false}, {true}};
    }

    @Test(dataProvider = "both")
    public void testMiss(boolean elementLocked) {
        FloatObjectStorage storage = createStorage(elementLocked);

        when(storage.load(42f)).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        FloatObjectCache cache = (FloatObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.size() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42f).equals("123");

        verify(storage).size();
        verify(storage, atLeast(1)).load(42f);
        verify(storage).save(42f, "123");
        if (elementLocked) {
            
                ((FloatObjectElementLockedStorage)verify(storage, atLeast(1))).lock(42f);
                ((FloatObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(42f);
            
        }
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;
    }

    @Test(dataProvider = "both")
    public void testHit(boolean elementLocked) {
        FloatObjectStorage storage = createStorage(elementLocked);

        FloatObjectCache cache = (FloatObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        when(storage.load(42f)).thenReturn("123");
        when(storage.size()).thenReturn(1);

        assert cache.size() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42f).equals("123");

        verify(storage).size();
        verify(storage, atLeast(1)).load(42f);
        if (elementLocked) {
            
                ((FloatObjectElementLockedStorage)verify(storage, atLeast(1))).lock(42f);
                ((FloatObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(42f);
            
        }
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;
    }

    @Test(dataProvider = "both")
    public void testClear(boolean elementLocked) {
        FloatObjectStorage storage = createStorage(elementLocked);

        FloatObjectCache cache = (FloatObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testSetDuringDependencyNodeOperations(boolean elementLocked) {
        FloatObjectStorage storage = createStorage(elementLocked);

        when(storage.load(42f)).thenReturn(Storage.UNDEFINED, "123");

        FloatObjectCalculatable calculatable = mock(FloatObjectCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", 42f)).thenThrow(new ResourceOccupied(r));

        FloatObjectCache cache = (FloatObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", calculatable, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42f).equals("123");

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).load(42f);
        if (elementLocked) {
            
                ((FloatObjectElementLockedStorage)verify(storage, atLeast(1))).lock(42f);
                ((FloatObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(42f);
            
        }
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", 42f);
        verifyNoMoreInteractions(calculatable);
    }

    @Test(dataProvider = "both")
    public void testResetStat(boolean elementLocked) {
        FloatObjectStorage storage = createStorage(elementLocked);

        when(storage.load(42f)).thenReturn(Storage.UNDEFINED);

        FloatObjectCache cache = (FloatObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42f).equals("123");

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load(42f);
        verify(storage).save(42f, "123");
        if (elementLocked) {
            
                ((FloatObjectElementLockedStorage)verify(storage, atLeast(1))).lock(42f);
                ((FloatObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(42f);
               
        }
        verifyNoMoreInteractions(storage);
    }

    private FloatObjectStorage createStorage(boolean elementLocked) {
        FloatObjectStorage storage = mock(elementLocked ? FloatObjectElementLockedStorage.class : FloatObjectStorage.class);
        if (elementLocked) {
            when(((FloatObjectElementLockedStorage)storage).getLock()).thenReturn(new ReentrantLock());
        }
        return storage;
    }

    @Test(dataProvider = "both")
    public void testTransparentStat(boolean elementLocked) {
        FloatObjectStorage storage = mock(elementLocked ? FloatObjectElementLockedStorage.class : FloatObjectStorage.class, withSettings().extraInterfaces(StatisticsHolder.class));

        FloatObjectCache cache = (FloatObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        cache.getStatistics();

        verify((StatisticsHolder)storage).getStatistics();
        verifyNoMoreInteractions(storage);
    }
}
