package com.maxifier.mxcache.impl.caches.storage;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.storage.ByteObjectStorage;
import com.maxifier.mxcache.storage.Storage;
import com.maxifier.mxcache.storage.elementlocked.ByteObjectElementLockedStorage;
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
public class ByteObjectCacheTest {
    private static final Signature SINGATURE = new Signature(byte.class, Object.class);

    private static final ByteObjectCalculatable CALCULATABLE = new ByteObjectCalculatable() {
        @Override
        public Object calculate(Object owner, byte o) {
            assert o == (byte)42;
            return "123";
        }
    };

    @DataProvider(name = "both")
    public Object[][] v200v210v219() {
        return new Object[][] {{false}, {true}};
    }

    @Test(dataProvider = "both")
    public void testMiss(boolean elementLocked) {
        ByteObjectStorage storage = createStorage(elementLocked);

        when(storage.load((byte)42)).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        ByteObjectCache cache = (ByteObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42).equals("123");

        verify(storage).size();
        verify(storage, atLeast(1)).load((byte)42);
        verify(storage).save((byte)42, "123");
        if (elementLocked) {
            
                ((ByteObjectElementLockedStorage)verify(storage, atLeast(1))).lock((byte)42);
                ((ByteObjectElementLockedStorage)verify(storage, atLeast(1))).unlock((byte)42);
            
        }
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;
    }

    @Test(dataProvider = "both")
    public void testHit(boolean elementLocked) {
        ByteObjectStorage storage = createStorage(elementLocked);

        ByteObjectCache cache = (ByteObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(storage.load((byte)42)).thenReturn("123");
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42).equals("123");

        verify(storage).size();
        verify(storage, atLeast(1)).load((byte)42);
        if (elementLocked) {
            
                ((ByteObjectElementLockedStorage)verify(storage, atLeast(1))).lock((byte)42);
                ((ByteObjectElementLockedStorage)verify(storage, atLeast(1))).unlock((byte)42);
            
        }
        verifyNoMoreInteractions(storage);

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;
    }

    @Test(dataProvider = "both")
    public void testClear(boolean elementLocked) {
        ByteObjectStorage storage = createStorage(elementLocked);

        ByteObjectCache cache = (ByteObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testSetDuringDependencyNodeOperations(boolean elementLocked) {
        ByteObjectStorage storage = createStorage(elementLocked);

        when(storage.load((byte)42)).thenReturn(Storage.UNDEFINED, "123");

        ByteObjectCalculatable calculatable = mock(ByteObjectCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", (byte)42)).thenThrow(new ResourceOccupied(r));

        ByteObjectCache cache = (ByteObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", calculatable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42).equals("123");

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).load((byte)42);
        if (elementLocked) {
            
                ((ByteObjectElementLockedStorage)verify(storage, atLeast(1))).lock((byte)42);
                ((ByteObjectElementLockedStorage)verify(storage, atLeast(1))).unlock((byte)42);
            
        }
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", (byte)42);
        verifyNoMoreInteractions(calculatable);
    }

    @Test(dataProvider = "both")
    public void testResetStat(boolean elementLocked) {
        ByteObjectStorage storage = createStorage(elementLocked);

        when(storage.load((byte)42)).thenReturn(Storage.UNDEFINED);

        ByteObjectCache cache = (ByteObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                    wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42).equals("123");

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load((byte)42);
        verify(storage).save((byte)42, "123");
        if (elementLocked) {
            
                ((ByteObjectElementLockedStorage)verify(storage, atLeast(1))).lock((byte)42);
                ((ByteObjectElementLockedStorage)verify(storage, atLeast(1))).unlock((byte)42);
               
        }
        verifyNoMoreInteractions(storage);
    }

    private ByteObjectStorage createStorage(boolean elementLocked) {
        ByteObjectStorage storage = mock(elementLocked ? ByteObjectElementLockedStorage.class : ByteObjectStorage.class);
        if (elementLocked) {
            when(((ByteObjectElementLockedStorage)storage).getLock()).thenReturn(new ReentrantLock());
        }
        return storage;
    }

    @Test(dataProvider = "both")
    public void testTransparentStat(boolean elementLocked) {
        ByteObjectStorage storage = mock(elementLocked ? ByteObjectElementLockedStorage.class : ByteObjectStorage.class, withSettings().extraInterfaces(StatisticsHolder.class));

        ByteObjectCache cache = (ByteObjectCache) Wrapping.getFactory(SINGATURE, SINGATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.getStatistics();

        verify((StatisticsHolder)storage).getStatistics();
        verifyNoMoreInteractions(storage);
    }
}
