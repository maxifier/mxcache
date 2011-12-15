package com.maxifier.mxcache.impl.caches.storage;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.storage.ObjectStorage;
import com.maxifier.mxcache.storage.Storage;
import com.maxifier.mxcache.storage.elementlocked.ObjectElementLockedStorage;
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
public class ObjectCacheTest {
    private static final Signature SINGATURE = new Signature(null, Object.class);

    private static final ObjectCalculatable CALCULATABLE = new ObjectCalculatable() {
        @Override
        public Object calculate(Object owner) {
            
            return "123";
        }
    };

    @DataProvider(name = "both")
    public Object[][] v200v210v219() {
        return new Object[][] {{false}, {true}};
    }

    @Test(dataProvider = "both")
    public void testMiss(boolean elementLocked) {
        ObjectStorage storage = createStorage(elementLocked);

        when(storage.load()).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        ObjectCache cache = (ObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate().equals("123");

        verify(storage).size();
        verify(storage, atLeast(1)).load();
        verify(storage).save("123");
        if (elementLocked) {
            
                ((ObjectElementLockedStorage)verify(storage, atLeast(1))).getLock();
            
        }
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;
    }

    @Test(dataProvider = "both")
    public void testHit(boolean elementLocked) {
        ObjectStorage storage = createStorage(elementLocked);

        ObjectCache cache = (ObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        when(storage.load()).thenReturn("123");
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate().equals("123");

        verify(storage).size();
        verify(storage, atLeast(1)).load();
        if (elementLocked) {
            
                ((ObjectElementLockedStorage)verify(storage, atLeast(1))).getLock();
            
        }
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;
    }

    @Test(dataProvider = "both")
    public void testClear(boolean elementLocked) {
        ObjectStorage storage = createStorage(elementLocked);

        ObjectCache cache = (ObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testSetDuringDependencyNodeOperations(boolean elementLocked) {
        ObjectStorage storage = createStorage(elementLocked);

        when(storage.load()).thenReturn(Storage.UNDEFINED, "123");

        ObjectCalculatable calculatable = mock(ObjectCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123")).thenThrow(new ResourceOccupied(r));

        ObjectCache cache = (ObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", calculatable, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate().equals("123");

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).load();
        if (elementLocked) {
            
                ((ObjectElementLockedStorage)verify(storage, atLeast(1))).getLock();
            
        }
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123");
        verifyNoMoreInteractions(calculatable);
    }

    @Test(dataProvider = "both")
    public void testResetStat(boolean elementLocked) {
        ObjectStorage storage = createStorage(elementLocked);

        when(storage.load()).thenReturn(Storage.UNDEFINED);

        ObjectCache cache = (ObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate().equals("123");

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load();
        verify(storage).save("123");
        if (elementLocked) {
            
                ((ObjectElementLockedStorage)verify(storage, atLeast(1))).getLock();
               
        }
        verifyNoMoreInteractions(storage);
    }

    private ObjectStorage createStorage(boolean elementLocked) {
        ObjectStorage storage = mock(elementLocked ? ObjectElementLockedStorage.class : ObjectStorage.class);
        if (elementLocked) {
            when(((ObjectElementLockedStorage)storage).getLock()).thenReturn(new ReentrantLock());
        }
        return storage;
    }

    @Test(dataProvider = "both")
    public void testTransparentStat(boolean elementLocked) {
        ObjectStorage storage = mock(elementLocked ? ObjectElementLockedStorage.class : ObjectStorage.class, withSettings().extraInterfaces(StatisticsHolder.class));

        ObjectCache cache = (ObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                wrap("123", CALCULATABLE, DependencyTracker.DUMMY_NODE, storage, new MutableStatisticsImpl());

        cache.getStatistics();

        verify((StatisticsHolder)storage).getStatistics();
        verifyNoMoreInteractions(storage);
    }
}
